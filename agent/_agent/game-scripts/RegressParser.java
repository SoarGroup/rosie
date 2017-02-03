// Generated from Regress.g4 by ANTLR 4.5
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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, SYMBOL=8, WORD=9, 
		NUMBER=10, PAREN=11, TERMINATOR=12, FAILED=13, COMMENT=14, BRACKETS=15, 
		WS=16;
	public static final int
		RULE_corpus = 0, RULE_block = 1, RULE_sentence = 2, RULE_expected = 3, 
		RULE_rhs = 4, RULE_attr = 5, RULE_value = 6, RULE_variable = 7;
	public static final String[] ruleNames = {
		"corpus", "block", "sentence", "expected", "rhs", "attr", "value", "variable"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "','", "'.'", "'('", "')'", "'^'", "'<'", "'>'", null, null, null, 
		null, null, "'#   FAILED!'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, "SYMBOL", "WORD", "NUMBER", 
		"PAREN", "TERMINATOR", "FAILED", "COMMENT", "BRACKETS", "WS"
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
			setState(17); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(16);
					block();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(19); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(24);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMENT) {
				{
				{
				setState(21);
				match(COMMENT);
				}
				}
				setState(26);
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
			setState(30);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMENT) {
				{
				{
				setState(27);
				match(COMMENT);
				}
				}
				setState(32);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(33);
			sentence();
			setState(35);
			_la = _input.LA(1);
			if (_la==FAILED) {
				{
				setState(34);
				match(FAILED);
				}
			}

			setState(38);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(37);
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
		public List<TerminalNode> WORD() { return getTokens(RegressParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(RegressParser.WORD, i);
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
			setState(40);
			match(WORD);
			setState(44);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(41);
					_la = _input.LA(1);
					if ( !(_la==T__0 || _la==WORD) ) {
					_errHandler.recoverInline(this);
					} else {
						consume();
					}
					}
					} 
				}
				setState(46);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			setState(48);
			_la = _input.LA(1);
			if (_la==T__1 || _la==TERMINATOR) {
				{
				setState(47);
				_la = _input.LA(1);
				if ( !(_la==T__1 || _la==TERMINATOR) ) {
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
		enterRule(_localctx, 6, RULE_expected);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(57); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(53);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMENT) {
						{
						{
						setState(50);
						match(COMMENT);
						}
						}
						setState(55);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(56);
					rhs();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(59); 
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
		enterRule(_localctx, 8, RULE_rhs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			match(T__2);
			setState(62);
			match(SYMBOL);
			setState(68); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(63);
				attr();
				setState(64);
				value();
				setState(66);
				_la = _input.LA(1);
				if (_la==COMMENT) {
					{
					setState(65);
					match(COMMENT);
					}
				}

				}
				}
				setState(70); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__4 );
			setState(72);
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
		enterRule(_localctx, 10, RULE_attr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			match(T__4);
			setState(84);
			switch (_input.LA(1)) {
			case NUMBER:
				{
				setState(75);
				match(NUMBER);
				}
				break;
			case WORD:
				{
				{
				setState(76);
				match(WORD);
				setState(81);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(77);
					match(T__1);
					setState(78);
					match(WORD);
					}
					}
					setState(83);
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
		enterRule(_localctx, 12, RULE_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			switch (_input.LA(1)) {
			case T__5:
				{
				setState(86);
				variable();
				}
				break;
			case SYMBOL:
				{
				setState(87);
				match(SYMBOL);
				}
				break;
			case WORD:
				{
				setState(88);
				match(WORD);
				}
				break;
			case NUMBER:
				{
				setState(89);
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
		enterRule(_localctx, 14, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			match(T__5);
			setState(93);
			match(WORD);
			setState(94);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\22c\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\3\2\6\2\24\n\2\r\2"+
		"\16\2\25\3\2\7\2\31\n\2\f\2\16\2\34\13\2\3\3\7\3\37\n\3\f\3\16\3\"\13"+
		"\3\3\3\3\3\5\3&\n\3\3\3\5\3)\n\3\3\4\3\4\7\4-\n\4\f\4\16\4\60\13\4\3\4"+
		"\5\4\63\n\4\3\5\7\5\66\n\5\f\5\16\59\13\5\3\5\6\5<\n\5\r\5\16\5=\3\6\3"+
		"\6\3\6\3\6\3\6\5\6E\n\6\6\6G\n\6\r\6\16\6H\3\6\3\6\3\7\3\7\3\7\3\7\3\7"+
		"\7\7R\n\7\f\7\16\7U\13\7\5\7W\n\7\3\b\3\b\3\b\3\b\5\b]\n\b\3\t\3\t\3\t"+
		"\3\t\3\t\2\2\n\2\4\6\b\n\f\16\20\2\4\4\2\3\3\13\13\4\2\4\4\16\16j\2\23"+
		"\3\2\2\2\4 \3\2\2\2\6*\3\2\2\2\b;\3\2\2\2\n?\3\2\2\2\fL\3\2\2\2\16\\\3"+
		"\2\2\2\20^\3\2\2\2\22\24\5\4\3\2\23\22\3\2\2\2\24\25\3\2\2\2\25\23\3\2"+
		"\2\2\25\26\3\2\2\2\26\32\3\2\2\2\27\31\7\20\2\2\30\27\3\2\2\2\31\34\3"+
		"\2\2\2\32\30\3\2\2\2\32\33\3\2\2\2\33\3\3\2\2\2\34\32\3\2\2\2\35\37\7"+
		"\20\2\2\36\35\3\2\2\2\37\"\3\2\2\2 \36\3\2\2\2 !\3\2\2\2!#\3\2\2\2\" "+
		"\3\2\2\2#%\5\6\4\2$&\7\17\2\2%$\3\2\2\2%&\3\2\2\2&(\3\2\2\2\')\5\b\5\2"+
		"(\'\3\2\2\2()\3\2\2\2)\5\3\2\2\2*.\7\13\2\2+-\t\2\2\2,+\3\2\2\2-\60\3"+
		"\2\2\2.,\3\2\2\2./\3\2\2\2/\62\3\2\2\2\60.\3\2\2\2\61\63\t\3\2\2\62\61"+
		"\3\2\2\2\62\63\3\2\2\2\63\7\3\2\2\2\64\66\7\20\2\2\65\64\3\2\2\2\669\3"+
		"\2\2\2\67\65\3\2\2\2\678\3\2\2\28:\3\2\2\29\67\3\2\2\2:<\5\n\6\2;\67\3"+
		"\2\2\2<=\3\2\2\2=;\3\2\2\2=>\3\2\2\2>\t\3\2\2\2?@\7\5\2\2@F\7\n\2\2AB"+
		"\5\f\7\2BD\5\16\b\2CE\7\20\2\2DC\3\2\2\2DE\3\2\2\2EG\3\2\2\2FA\3\2\2\2"+
		"GH\3\2\2\2HF\3\2\2\2HI\3\2\2\2IJ\3\2\2\2JK\7\6\2\2K\13\3\2\2\2LV\7\7\2"+
		"\2MW\7\f\2\2NS\7\13\2\2OP\7\4\2\2PR\7\13\2\2QO\3\2\2\2RU\3\2\2\2SQ\3\2"+
		"\2\2ST\3\2\2\2TW\3\2\2\2US\3\2\2\2VM\3\2\2\2VN\3\2\2\2W\r\3\2\2\2X]\5"+
		"\20\t\2Y]\7\n\2\2Z]\7\13\2\2[]\7\f\2\2\\X\3\2\2\2\\Y\3\2\2\2\\Z\3\2\2"+
		"\2\\[\3\2\2\2]\17\3\2\2\2^_\7\b\2\2_`\7\13\2\2`a\7\t\2\2a\21\3\2\2\2\20"+
		"\25\32 %(.\62\67=DHSV\\";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}