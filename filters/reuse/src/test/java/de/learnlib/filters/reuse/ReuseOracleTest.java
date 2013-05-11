package de.learnlib.filters.reuse;

import de.learnlib.filters.reuse.api.ExecutableSymbol;
import de.learnlib.filters.reuse.api.SystemState;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReuseOracleTest {
	private final static String ACK = "ACK";
	private final static String NAK = "NAK";

	private final MySymbol i1 = new MySymbol(this, 1);
	private final MySymbol i2 = new MySymbol(this, 2);
	private final MySymbol i3 = new MySymbol(this, 3);

	private ReuseOracle<MySymbol,String> reuseOracle;

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

		ExecutableOracle<MySymbol,String> executableOracle = new ExecutableOracle<>(reset);

		reuseOracle = new ReuseOracle<>(executableOracle);
	}

	@Test
	public void test() {
		reuseOracle.getReuseTree().addFailureOutputSymbol("NAK");

		Word<MySymbol> query = Word.fromSymbols(i1);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracle.NeededAction.RESET_NECCESSARY);
		Word<String> output = reuseOracle.executeFullQuery(query); /* ACK */
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
