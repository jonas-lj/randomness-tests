package dk.alexandra.randomnesstests;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class BirthdaySpacingsTest {

  @Test
  public void testBirthdaySpacingsBadRNG() {
    Supplier<BigInteger> badrng = new BadRNG();

    BirthdaySpacings bday = new BirthdaySpacings(badrng, BigInteger.ONE.shiftLeft(32));
    double p = bday.test(4096, 5000);

    assertTrue(p < 0.05);
  }

  @Test
  public void testBirthdaySpacingsGoodRNG() {
    Random random = new Random(1234);
    Supplier<BigInteger> goodrng = () -> new BigInteger(32, random);

    BirthdaySpacings bday = new BirthdaySpacings(goodrng, BigInteger.ONE.shiftLeft(32));
    double p = bday.test(4096, 5000);

    assertTrue(p > 0.05);
  }


  /** A bad RNG based (https://en.wikipedia.org/wiki/Linear_congruential_generator) */
  public static class BadRNG implements Supplier<BigInteger> {

    private BigInteger previous = BigInteger.ONE;

    @Override
    public BigInteger get() {
      // x(n)=214013*x(n-1)+2531011 mod 2ˆ32
      previous = previous.multiply(BigInteger.valueOf(214013)).add(BigInteger.valueOf(2531011)).mod(BigInteger.ONE.shiftLeft(32));
      return previous;
    }
  }
}