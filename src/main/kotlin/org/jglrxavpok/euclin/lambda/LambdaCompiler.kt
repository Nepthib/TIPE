package org.jglrxavpok.euclin.lambda

import org.jglrxavpok.euclin.Argument
import org.jglrxavpok.euclin.FunctionCompiler
import org.jglrxavpok.euclin.FunctionSignature
import org.jglrxavpok.euclin.grammar.EuclinBaseVisitor
import org.jglrxavpok.euclin.grammar.EuclinParser
import org.jglrxavpok.euclin.types.ExpressionTranslator
import org.jglrxavpok.euclin.types.RealType
import org.objectweb.asm.ClassWriter

class LambdaCompiler(val classWriter: ClassWriter, val ownerClass: String, val availableFunctions: Map<String, FunctionSignature>):
    EuclinBaseVisitor<FunctionSignature?>() {

    private var lambdaID = 0
    private var resultMap = hashMapOf<String, FunctionSignature>()
    private val translator = ExpressionTranslator(availableFunctions)
    private val alreadyCompiled = hashMapOf<String, FunctionSignature>()

    fun compileLambdas(code: EuclinParser.CodeBlockContext): Map<String, FunctionSignature> {
        resultMap = hashMapOf()
        code.instructions().forEach { it.accept(this) }
        return resultMap
    }

    override fun visitLambdaFunctionExpr(ctx: EuclinParser.LambdaFunctionExprContext): FunctionSignature? {
        val functionExpression = ctx.expression()

        println("COMPILING ${functionExpression.text}")
        // on ne compile pas deux fois la même fonction!
        if(alreadyCompiled.containsKey(functionExpression.text)) {
            resultMap[functionExpression.text] = alreadyCompiled[functionExpression.text]!!
            println(">> ${functionExpression.text}")
            return null
        }
        val function = translator.translateLambdaExpression(functionExpression)
        val returnType = function.expression.type

        // si l'expression n'est que '_', on change le nom
        val name = if (functionExpression.text.trim() == "_") "_lambda_identity" else "_lambda_$lambdaID"
        val lambdaSignature = FunctionSignature(name, listOf(Argument("_", RealType)), returnType, ownerClass) // TODO: Meilleur nom?
        lambdaID++
        val functionBody = generateLambdaBody(functionExpression)

        alreadyCompiled[functionExpression.text] = lambdaSignature
        resultMap[functionExpression.text] = lambdaSignature

        val funcCompiler = FunctionCompiler(classWriter, lambdaSignature, availableFunctions, resultMap)
        funcCompiler.visitFunctionCodeBlock(functionBody)

        println(">> ${functionExpression.text} - ${lambdaSignature.name}")
        return lambdaSignature
    }

    private fun generateLambdaBody(instruction: EuclinParser.ExpressionContext): EuclinParser.FunctionCodeBlockContext {
        val result = EuclinParser.FunctionCodeBlockContext(null, -1)
        val instructions = EuclinParser.FunctionInstructionsContext()
        result.addChild(instructions)

        val returnInstructionWrapper = EuclinParser.ReturnFuncInstructionContext(instructions)
        returnInstructionWrapper.addChild(instruction)

        instructions.addChild(returnInstructionWrapper)
        return result
    }
}