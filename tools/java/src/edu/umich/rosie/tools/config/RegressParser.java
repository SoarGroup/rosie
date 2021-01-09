// Generated from src/edu/umich/rosie/tools/config/Regress.g4 by ANTLR 4.5

package edu.umich.rosie.tools.config;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RegressParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, TIME=8, QUOTE=9, 
		SYMBOL=10, WORD=11, NUMBER=12, PAREN=13, TERMINATOR=14, COMMA=15, FAILED=16, 
		COMMENT=17, BRACKETS=18, WS=19;
	public static final int
		RULE_corpus = 0, RULE_block = 1, RULE_sentence = 2, RULE_sentenceWord = 3, 
		RULE_expected = 4, RULE_rhs = 5, RULE_attr = 6, RULE_value = 7, RULE_variable = 8;
	public static final String[] ruleNames = {
		"corpus", "block", "sentence", "sentenceWord", "expected", "rhs", "attr", 
		"value", "variable"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'('", "'@'", "')'", "'^'", "'<'", "'>'", null, null, null, 
		null, null, null, null, "','", "'#   FAILED!'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, "TIME", "QUOTE", "SYMBOL", 
		"WORD", "NUMBER", "PAREN", "TERMINATOR", "COMMA", "FAILED", "COMMENT", 
		"BRACKETS", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Regress.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RegressParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class CorpusContext extends ParserRuleContext {
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public List<TerminalNode> COMMENT() { return getTokens(RegressParser.COMMENT); }
		public TerminalNode COMMENT(int i) {
			return getToken(RegressParser.COMMENT, i);
		}
		public CorpusContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_corpus; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterCorpus(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitCorpus(this);
		}
	}

	public final CorpusContext corpus() throws RecognitionException {
		CorpusContext _localctx = new CorpusContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_corpus);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(19); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(18);
					block();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(21); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(26);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMENT) {
				{
				{
				setState(23);
				match(COMMENT);
				}
				}
				setState(28);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockContext extends ParserRuleContext {
		public SentenceContext sentence() {
			return getRuleContext(SentenceContext.class,0);
		}
		public List<TerminalNode> COMMENT() { return getTokens(RegressParser.COMMENT); }
		public TerminalNode COMMENT(int i) {
			return getToken(RegressParser.COMMENT, i);
		}
		public TerminalNode FAILED() { return getToken(RegressParser.FAILED, 0); }
		public ExpectedContext expected() {
			return getRuleContext(ExpectedContext.class,0);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitBlock(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMENT) {
				{
				{
				setState(29);
				match(COMMENT);
				}
				}
				setState(34);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(35);
			sentence();
			setState(37);
			_la = _input.LA(1);
			if (_la==FAILED) {
				{
				setState(36);
				match(FAILED);
				}
			}

			setState(40);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(39);
				expected();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SentenceContext extends ParserRuleContext {
		public List<SentenceWordContext> sentenceWord() {
			return getRuleContexts(SentenceWordContext.class);
		}
		public SentenceWordContext sentenceWord(int i) {
			return getRuleContext(SentenceWordContext.class,i);
		}
		public TerminalNode TERMINATOR() { return getToken(RegressParser.TERMINATOR, 0); }
		public SentenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sentence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterSentence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitSentence(this);
		}
	}

	public final SentenceContext sentence() throws RecognitionException {
		SentenceContext _localctx = new SentenceContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_sentence);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(42);
			sentenceWord();
			setState(46);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(43);
					sentenceWord();
					}
					} 
				}
				setState(48);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			setState(50);
			_la = _input.LA(1);
			if (_la==T__0 || _la==TERMINATOR) {
				{
				setState(49);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==TERMINATOR) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SentenceWordContext extends ParserRuleContext {
		public TerminalNode QUOTE() { return getToken(RegressParser.QUOTE, 0); }
		public TerminalNode TIME() { return getToken(RegressParser.TIME, 0); }
		public TerminalNode WORD() { return getToken(RegressParser.WORD, 0); }
		public TerminalNode COMMA() { return getToken(RegressParser.COMMA, 0); }
		public SentenceWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sentenceWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterSentenceWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitSentenceWord(this);
		}
	}

	public final SentenceWordContext sentenceWord() throws RecognitionException {
		SentenceWordContext _localctx = new SentenceWordContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_sentenceWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TIME) | (1L << QUOTE) | (1L << WORD) | (1L << COMMA))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpectedContext extends ParserRuleContext {
		public List<RhsContext> rhs() {
			return getRuleContexts(RhsContext.class);
		}
		public RhsContext rhs(int i) {
			return getRuleContext(RhsContext.class,i);
		}
		public List<TerminalNode> COMMENT() { return getTokens(RegressParser.COMMENT); }
		public TerminalNode COMMENT(int i) {
			return getToken(RegressParser.COMMENT, i);
		}
		public ExpectedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expected; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterExpected(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitExpected(this);
		}
	}

	public final ExpectedContext expected() throws RecognitionException {
		ExpectedContext _localctx = new ExpectedContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_expected);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(61); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(57);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMENT) {
						{
						{
						setState(54);
						match(COMMENT);
						}
						}
						setState(59);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(60);
					rhs();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(63); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RhsContext extends ParserRuleContext {
		public TerminalNode SYMBOL() { return getToken(RegressParser.SYMBOL, 0); }
		public List<AttrContext> attr() {
			return getRuleContexts(AttrContext.class);
		}
		public AttrContext attr(int i) {
			return getRuleContext(AttrContext.class,i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<TerminalNode> COMMENT() { return getTokens(RegressParser.COMMENT); }
		public TerminalNode COMMENT(int i) {
			return getToken(RegressParser.COMMENT, i);
		}
		public RhsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rhs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterRhs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitRhs(this);
		}
	}

	public final RhsContext rhs() throws RecognitionException {
		RhsContext _localctx = new RhsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_rhs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(T__1);
			setState(67);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(66);
				match(T__2);
				}
			}

			setState(69);
			match(SYMBOL);
			setState(75); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(70);
				attr();
				setState(71);
				value();
				setState(73);
				_la = _input.LA(1);
				if (_la==COMMENT) {
					{
					setState(72);
					match(COMMENT);
					}
				}

				}
				}
				setState(77); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__4 );
			setState(79);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttrContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(RegressParser.NUMBER, 0); }
		public List<TerminalNode> WORD() { return getTokens(RegressParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(RegressParser.WORD, i);
		}
		public AttrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterAttr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitAttr(this);
		}
	}

	public final AttrContext attr() throws RecognitionException {
		AttrContext _localctx = new AttrContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_attr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			match(T__4);
			setState(91);
			switch (_input.LA(1)) {
			case NUMBER:
				{
				setState(82);
				match(NUMBER);
				}
				break;
			case WORD:
				{
				{
				setState(83);
				match(WORD);
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__0) {
					{
					{
					setState(84);
					match(T__0);
					setState(85);
					match(WORD);
					}
					}
					setState(90);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode SYMBOL() { return getToken(RegressParser.SYMBOL, 0); }
		public TerminalNode WORD() { return getToken(RegressParser.WORD, 0); }
		public TerminalNode NUMBER() { return getToken(RegressParser.NUMBER, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitValue(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			switch (_input.LA(1)) {
			case T__5:
				{
				setState(93);
				variable();
				}
				break;
			case T__2:
			case SYMBOL:
				{
				setState(95);
				_la = _input.LA(1);
				if (_la==T__2) {
					{
					setState(94);
					match(T__2);
					}
				}

				setState(97);
				match(SYMBOL);
				}
				break;
			case WORD:
				{
				setState(98);
				match(WORD);
				}
				break;
			case NUMBER:
				{
				setState(99);
				match(NUMBER);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableContext extends ParserRuleContext {
		public TerminalNode WORD() { return getToken(RegressParser.WORD, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegressListener ) ((RegressListener)listener).exitVariable(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			match(T__5);
			setState(103);
			match(WORD);
			setState(104);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\25m\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\6\2\26"+
		"\n\2\r\2\16\2\27\3\2\7\2\33\n\2\f\2\16\2\36\13\2\3\3\7\3!\n\3\f\3\16\3"+
		"$\13\3\3\3\3\3\5\3(\n\3\3\3\5\3+\n\3\3\4\3\4\7\4/\n\4\f\4\16\4\62\13\4"+
		"\3\4\5\4\65\n\4\3\5\3\5\3\6\7\6:\n\6\f\6\16\6=\13\6\3\6\6\6@\n\6\r\6\16"+
		"\6A\3\7\3\7\5\7F\n\7\3\7\3\7\3\7\3\7\5\7L\n\7\6\7N\n\7\r\7\16\7O\3\7\3"+
		"\7\3\b\3\b\3\b\3\b\3\b\7\bY\n\b\f\b\16\b\\\13\b\5\b^\n\b\3\t\3\t\5\tb"+
		"\n\t\3\t\3\t\3\t\5\tg\n\t\3\n\3\n\3\n\3\n\3\n\2\2\13\2\4\6\b\n\f\16\20"+
		"\22\2\4\4\2\3\3\20\20\5\2\n\13\r\r\21\21u\2\25\3\2\2\2\4\"\3\2\2\2\6,"+
		"\3\2\2\2\b\66\3\2\2\2\n?\3\2\2\2\fC\3\2\2\2\16S\3\2\2\2\20f\3\2\2\2\22"+
		"h\3\2\2\2\24\26\5\4\3\2\25\24\3\2\2\2\26\27\3\2\2\2\27\25\3\2\2\2\27\30"+
		"\3\2\2\2\30\34\3\2\2\2\31\33\7\23\2\2\32\31\3\2\2\2\33\36\3\2\2\2\34\32"+
		"\3\2\2\2\34\35\3\2\2\2\35\3\3\2\2\2\36\34\3\2\2\2\37!\7\23\2\2 \37\3\2"+
		"\2\2!$\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#%\3\2\2\2$\"\3\2\2\2%\'\5\6\4\2&"+
		"(\7\22\2\2\'&\3\2\2\2\'(\3\2\2\2(*\3\2\2\2)+\5\n\6\2*)\3\2\2\2*+\3\2\2"+
		"\2+\5\3\2\2\2,\60\5\b\5\2-/\5\b\5\2.-\3\2\2\2/\62\3\2\2\2\60.\3\2\2\2"+
		"\60\61\3\2\2\2\61\64\3\2\2\2\62\60\3\2\2\2\63\65\t\2\2\2\64\63\3\2\2\2"+
		"\64\65\3\2\2\2\65\7\3\2\2\2\66\67\t\3\2\2\67\t\3\2\2\28:\7\23\2\298\3"+
		"\2\2\2:=\3\2\2\2;9\3\2\2\2;<\3\2\2\2<>\3\2\2\2=;\3\2\2\2>@\5\f\7\2?;\3"+
		"\2\2\2@A\3\2\2\2A?\3\2\2\2AB\3\2\2\2B\13\3\2\2\2CE\7\4\2\2DF\7\5\2\2E"+
		"D\3\2\2\2EF\3\2\2\2FG\3\2\2\2GM\7\f\2\2HI\5\16\b\2IK\5\20\t\2JL\7\23\2"+
		"\2KJ\3\2\2\2KL\3\2\2\2LN\3\2\2\2MH\3\2\2\2NO\3\2\2\2OM\3\2\2\2OP\3\2\2"+
		"\2PQ\3\2\2\2QR\7\6\2\2R\r\3\2\2\2S]\7\7\2\2T^\7\16\2\2UZ\7\r\2\2VW\7\3"+
		"\2\2WY\7\r\2\2XV\3\2\2\2Y\\\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2[^\3\2\2\2\\Z\3"+
		"\2\2\2]T\3\2\2\2]U\3\2\2\2^\17\3\2\2\2_g\5\22\n\2`b\7\5\2\2a`\3\2\2\2"+
		"ab\3\2\2\2bc\3\2\2\2cg\7\f\2\2dg\7\r\2\2eg\7\16\2\2f_\3\2\2\2fa\3\2\2"+
		"\2fd\3\2\2\2fe\3\2\2\2g\21\3\2\2\2hi\7\b\2\2ij\7\r\2\2jk\7\t\2\2k\23\3"+
		"\2\2\2\22\27\34\"\'*\60\64;AEKOZ]af";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}