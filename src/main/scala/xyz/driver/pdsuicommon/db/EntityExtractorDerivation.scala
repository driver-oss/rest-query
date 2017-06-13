package xyz.driver.pdsuicommon.db

import java.sql.ResultSet

import io.getquill.NamingStrategy
import io.getquill.dsl.EncodingDsl

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait EntityExtractorDerivation[Naming <: NamingStrategy] {
  this: EncodingDsl =>

  /**
    * Simple Quill extractor derivation for [[T]]
    * Only case classes available. Type parameters is not supported
    */
  def entityExtractor[T]: (ResultSet => T) = macro EntityExtractorDerivation.impl[T]
}

object EntityExtractorDerivation {
  def impl[T: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._
    val namingStrategy = c.prefix.actualType
      .baseType(c.weakTypeOf[EntityExtractorDerivation[NamingStrategy]].typeSymbol)
      .typeArgs
      .head
      .typeSymbol
      .companion
    val functionBody = {
      val tpe = weakTypeOf[T]
      val resultOpt = tpe.decls.collectFirst {
        // Find first constructor of T
        case cons: MethodSymbol if cons.isConstructor =>
          // Create param list for constructor
          val params = cons.paramLists.flatten.map { param =>
            val t = param.typeSignature
            val paramName = param.name.toString
            val col = q"$namingStrategy.column($paramName)"
            // Resolve implicit decoders (from SqlContext) and apply ResultSet for each
            val d = q"implicitly[${c.prefix}.Decoder[$t]]"
            // Minus 1 cause Quill JDBC decoders make plus one.
            // ¯\_(ツ)_/¯
            val i = q"row.findColumn($col) - 1"
            val decoderName = TermName(paramName + "Decoder")
            val valueName = TermName(paramName + "Value")
            (
              q"val $decoderName = $d",
              q"val $valueName = $decoderName($i, row)",
              valueName
            )
          }
          // Call constructor with param list
          q"""
            ..${params.map(_._1)}
            ..${params.map(_._2)}
            new $tpe(..${params.map(_._3)})
          """
      }
      resultOpt match {
        case Some(result) => result
        case None => c.abort(c.enclosingPosition,
          s"Can not derive extractor for $tpe. Constructor not found.")
      }
    }
    q"(row: java.sql.ResultSet) => $functionBody"
  }
}
