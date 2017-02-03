// Generated from Regress.g4 by ANTLR 4.5
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RegressParser}.
 */
public interface RegressListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RegressParser#corpus}.
	 * @param ctx the parse tree
	 */
	void enterCorpus(RegressParser.CorpusContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#corpus}.
	 * @param ctx the parse tree
	 */
	void exitCorpus(RegressParser.CorpusContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegressParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(RegressParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(RegressParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegressParser#sentence}.
	 * @param ctx the parse tree
	 */
	void enterSentence(RegressParser.SentenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#sentence}.
	 * @param ctx the parse tree
	 */
	void exitSentence(RegressParser.SentenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegressParser#expected}.
	 * @param ctx the parse tree
	 */
	void enterExpected(RegressParser.ExpectedContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#expected}.
	 * @param ctx the parse tree
	 */
	void exitExpected(RegressParser.ExpectedContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegressParser#rhs}.
	 * @param ctx the parse tree
	 */
	void enterRhs(RegressParser.RhsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#rhs}.
	 * @param ctx the parse tree
	 */
	void exitRhs(RegressParser.RhsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegressParser#attr}.
	 * @param ctx the parse tree
	 */
	void enterAttr(RegressParser.AttrContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#attr}.
	 * @param ctx the parse tree
	 */
	void exitAttr(RegressParser.AttrContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegressParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(RegressParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(RegressParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegressParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(RegressParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegressParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(RegressParser.VariableContext ctx);
}