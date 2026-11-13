package ru.dimsuz.vanilla.processor

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.combine
import com.github.michaelbull.result.flatMap
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import ru.dimsuz.vanilla.ValidatedAs

internal class VanillaProcessor(
  private val processingEnv: SymbolProcessorEnvironment,
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val validatedAsSymbols = resolver
      .getSymbolsWithAnnotation(ValidatedAs::class.qualifiedName.orEmpty())
      .filterIsInstance<KSClassDeclaration>()
      .toList()

    // Symbols are analyzed and generated within the round they are received in: KSP2 invalidates
    // them between rounds. Symbols with unresolved types are deferred to the next round.
    val (ready, deferred) = validatedAsSymbols.partition { it.validate() }
    if (ready.isEmpty()) return deferred

    val modelPairs = mutableListOf<Result<ModelPair, Error>>()
    ready.forEach { ksClassDeclaration ->
      ksClassDeclaration.accept(
        ValidateAsVisitor { modelPairs += it },
        Unit
      )
    }

    val result = modelPairs
      .combine()
      .flatMap { modelPairList ->
        modelPairList.map { findMatchingProperties(it) }.combine()
      }
      .flatMap { propertyMappings ->
        propertyMappings.map { generateValidator(processingEnv, it) }.combine()
      }

    if (result is Err) {
      processingEnv.logger.error(result.error)
    }
    return deferred
  }
}
