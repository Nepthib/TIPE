package org.jglrxavpok.euclin

import org.jglrxavpok.euclin.grammar.EuclinBaseVisitor
import org.jglrxavpok.euclin.grammar.EuclinParser

/**
 * Vérifies si une expression donnée est constante (ie tous les appels de fonctions sont faits vers des fonctions *pures* et les arguments sont constants)
 */
class ConstantChecker(val availableFunctions: Map<String, FunctionSignature>): EuclinBaseVisitor<Boolean>() {
    fun assertConstant(constantExpr: EuclinParser.ExpressionContext) {
        assert(visit(constantExpr)) { "L'expression '${constantExpr.text}' n'est pas une constante" }
    }

    override fun visitBoolTrueExpr(ctx: EuclinParser.BoolTrueExprContext?): Boolean {
        return true
    }

    override fun visitBoolFalseExpr(ctx: EuclinParser.BoolFalseExprContext?): Boolean {
        return true
    }

    override fun visitFunctionCall(call: EuclinParser.FunctionCallContext): Boolean {
        val signature = availableFunctions[call.Identifier().text]!!
        return signature.pure && call.expression().all { visit(it) }
    }

    override fun visitLambdaVarExpr(ctx: EuclinParser.LambdaVarExprContext?): Boolean {
        return false
    }

    override fun visitFloatExpr(ctx: EuclinParser.FloatExprContext?): Boolean {
        return true
    }

    override fun visitIntExpr(ctx: EuclinParser.IntExprContext?): Boolean {
        return true
    }

    override fun aggregateResult(aggregate: Boolean, nextResult: Boolean): Boolean {
        return aggregate && nextResult // On vérifie que *tous* les éléments sont purs
    }

    override fun defaultResult(): Boolean {
        return false
    }
}