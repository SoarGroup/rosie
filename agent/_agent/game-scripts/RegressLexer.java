// Generated from Regress.g4 by ANTLR 4.5
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RegressLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, SYMBOL=8, WORD=9, 
		NUMBER=10, PAREN=11, TERMINATOR=12, FAILED=13, COMMENT=14, BRACKETS=15, 
		WS=16;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "SYMBOL", "WORD", 
		"NUMBER", "DIGIT", "PAREN", "TERMINATOR", "FAILED", "COMMENT", "BRACKETS", 
		"WS"
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


	public RegressLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Regress.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\22\u0087\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\5\t"+
		"\65\n\t\3\t\3\t\6\t9\n\t\r\t\16\t:\3\n\3\n\7\n?\n\n\f\n\16\nB\13\n\3\13"+
		"\5\13E\n\13\3\13\3\13\6\13I\n\13\r\13\16\13J\3\13\6\13N\n\13\r\13\16\13"+
		"O\3\13\3\13\7\13T\n\13\f\13\16\13W\13\13\5\13Y\n\13\5\13[\n\13\3\f\3\f"+
		"\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\20\3\20\7\20q\n\20\f\20\16\20t\13\20\3\21\3\21\7\21x\n\21\f"+
		"\21\16\21{\13\21\3\21\3\21\3\21\3\21\3\22\6\22\u0082\n\22\r\22\16\22\u0083"+
		"\3\22\3\22\3y\2\23\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\2"+
		"\31\r\33\16\35\17\37\20!\21#\22\3\2\t\4\2C\\c|\3\2\62;\b\2))//\62;C\\"+
		"aac|\3\2*+\5\2##==AA\4\2\f\f\17\17\5\2\13\f\17\17\"\"\u0091\2\3\3\2\2"+
		"\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3"+
		"\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2"+
		"\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\3%\3\2\2\2\5\'\3\2"+
		"\2\2\7)\3\2\2\2\t+\3\2\2\2\13-\3\2\2\2\r/\3\2\2\2\17\61\3\2\2\2\21\64"+
		"\3\2\2\2\23<\3\2\2\2\25D\3\2\2\2\27\\\3\2\2\2\31^\3\2\2\2\33`\3\2\2\2"+
		"\35b\3\2\2\2\37n\3\2\2\2!u\3\2\2\2#\u0081\3\2\2\2%&\7.\2\2&\4\3\2\2\2"+
		"\'(\7\60\2\2(\6\3\2\2\2)*\7*\2\2*\b\3\2\2\2+,\7+\2\2,\n\3\2\2\2-.\7`\2"+
		"\2.\f\3\2\2\2/\60\7>\2\2\60\16\3\2\2\2\61\62\7@\2\2\62\20\3\2\2\2\63\65"+
		"\7B\2\2\64\63\3\2\2\2\64\65\3\2\2\2\65\66\3\2\2\2\668\t\2\2\2\679\t\3"+
		"\2\28\67\3\2\2\29:\3\2\2\2:8\3\2\2\2:;\3\2\2\2;\22\3\2\2\2<@\t\2\2\2="+
		"?\t\4\2\2>=\3\2\2\2?B\3\2\2\2@>\3\2\2\2@A\3\2\2\2A\24\3\2\2\2B@\3\2\2"+
		"\2CE\7/\2\2DC\3\2\2\2DE\3\2\2\2EZ\3\2\2\2FH\7\60\2\2GI\5\27\f\2HG\3\2"+
		"\2\2IJ\3\2\2\2JH\3\2\2\2JK\3\2\2\2K[\3\2\2\2LN\5\27\f\2ML\3\2\2\2NO\3"+
		"\2\2\2OM\3\2\2\2OP\3\2\2\2PX\3\2\2\2QU\7\60\2\2RT\5\27\f\2SR\3\2\2\2T"+
		"W\3\2\2\2US\3\2\2\2UV\3\2\2\2VY\3\2\2\2WU\3\2\2\2XQ\3\2\2\2XY\3\2\2\2"+
		"Y[\3\2\2\2ZF\3\2\2\2ZM\3\2\2\2[\26\3\2\2\2\\]\t\3\2\2]\30\3\2\2\2^_\t"+
		"\5\2\2_\32\3\2\2\2`a\t\6\2\2a\34\3\2\2\2bc\7%\2\2cd\7\"\2\2de\7\"\2\2"+
		"ef\7\"\2\2fg\7H\2\2gh\7C\2\2hi\7K\2\2ij\7N\2\2jk\7G\2\2kl\7F\2\2lm\7#"+
		"\2\2m\36\3\2\2\2nr\7%\2\2oq\n\7\2\2po\3\2\2\2qt\3\2\2\2rp\3\2\2\2rs\3"+
		"\2\2\2s \3\2\2\2tr\3\2\2\2uy\7]\2\2vx\13\2\2\2wv\3\2\2\2x{\3\2\2\2yz\3"+
		"\2\2\2yw\3\2\2\2z|\3\2\2\2{y\3\2\2\2|}\7_\2\2}~\3\2\2\2~\177\b\21\2\2"+
		"\177\"\3\2\2\2\u0080\u0082\t\b\2\2\u0081\u0080\3\2\2\2\u0082\u0083\3\2"+
		"\2\2\u0083\u0081\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0085\3\2\2\2\u0085"+
		"\u0086\b\22\2\2\u0086$\3\2\2\2\17\2\64:@DJOUXZry\u0083\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}