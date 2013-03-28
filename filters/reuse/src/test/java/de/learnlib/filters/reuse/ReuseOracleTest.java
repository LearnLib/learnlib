package de.learnlib.filters.reuse;


import de.learnlib.filters.reuse.api.ExecutableSymbol;
import de.learnlib.filters.reuse.api.SystemState;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReuseOracleTest {
	private final static Symbol ACK = new Symbol("ACK");
	private final static Symbol NAK = new Symbol("NAK");

	private final Symbol i1 = new Symbol(new MySymbol(this, 1));
	private final Symbol i2 = new Symbol(new MySymbol(this, 2));
	private final Symbol i3 = new Symbol(new MySymbol(this, 3));

	private ReuseOracle reuseOracle;

	/**
	 * {@inheritDoc}.
	 */
	@BeforeClass
	protected void setUp() {
		ExecutableSymbol reset = new ExecutableSymbol() {
			@Override
			public String execute(SystemState state) {
				state.put("VALUE", null);
				return "ok";
			}
		};

		ExecutableOracle executableOracle = new ExecutableOracle(reset);

		reuseOracle = new ReuseOracle(executableOracle);
	}

	@Test
	public void test() {
		reuseOracle.getReuseTree().addFailureOutputSymbol("NAK");

		Word<Symbol> query = Word.fromSymbols(i1);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracle.NeededAction.RESET_NECCESSARY);
		Word<Symbol> output = reuseOracle.executeFullQuery(query); /* ACK */
		Assert.assertEquals(output, Word.fromSymbols(ACK));

		query = Word.fromSymbols(i1, i3);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracle.NeededAction.PREPARE_PREFIX);
		output = reuseOracle.executeSuffixFromQuery(query); /* ACK NAK */
		Assert.assertEquals(output, Word.fromSymbols(ACK, NAK));

		query = Word.fromSymbols(i1, i2);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracle.NeededAction.PREPARE_PREFIX);
		output = reuseOracle.executeSuffixFromQuery(query); /* ACK ACK */
		Assert.assertEquals(output, Word.fromSymbols(ACK, ACK));

		// should be already known, pump reflexive edge
		query = Word.fromSymbols(i1, i3, i3, i2);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracle.NeededAction.ALREADY_KNOWN);
		output = reuseOracle.answerQuery(query); /* ACK NAK NAK ACK */
		Assert.assertEquals(output, Word.fromSymbols(ACK, NAK, NAK, ACK));
	}

}
