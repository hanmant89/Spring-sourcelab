package rewards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import common.money.MonetaryAmount;

import config.RewardsConfig;

/**
 * A system test that verifies the components of the RewardNetwork application work together to reward for dining
 * successfully. Uses Spring to bootstrap the application for use in a test environment.
 */
public class RewardNetworkTests  {

	/**
	 * The object being tested.
	 */
	private RewardNetwork rewardNetwork;
	private AbstractApplicationContext context;
	
	@Before
	public void setUp() {
		// Create the test configuration for the application from two classes:
		context = 
			new AnnotationConfigApplicationContext( 
				RewardsConfig.class, TestInfrastructureConfig.class );

		// Get the bean to use to invoke the application
		rewardNetwork = context.getBean(RewardNetwork.class);

		//	Register a shutdown hook with the JVM.
		//	This causes the JVM to call the close logic on the application
		//	context whenever the JVM is closing.  
		context.registerShutdownHook();
	}
	


	@Test
	public void rewardForDining() {
		// create a new dining of 100.00 charged to credit card '1234123412341234' by merchant '123457890' as test input
		Dining dining = Dining.createDining("100.00", "1234123412341234", "1234567890");

		// call the 'rewardNetwork' to test its rewardAccountFor(Dining) method
		RewardConfirmation confirmation = rewardNetwork.rewardAccountFor(dining);

		// assert the expected reward confirmation results
		assertNotNull(confirmation);
		assertNotNull(confirmation.getConfirmationNumber());

		// assert an account contribution was made
		AccountContribution contribution = confirmation.getAccountContribution();
		assertNotNull(contribution);

		// the contribution account number should be '123456789'
		assertEquals("123456789", contribution.getAccountNumber());

		// the total contribution amount should be 8.00 (8% of 100.00)
		assertEquals(MonetaryAmount.valueOf("8.00"), contribution.getAmount());

		// the total contribution amount should have been split into 2 distributions
		assertEquals(2, contribution.getDistributions().size());

		// each distribution should be 4.00 (as both have a 50% allocation)
		assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Annabelle").getAmount());
		assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Corgan").getAmount());
	}
}
