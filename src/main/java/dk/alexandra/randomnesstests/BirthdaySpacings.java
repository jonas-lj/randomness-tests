package dk.alexandra.randomnesstests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

/**
 * Randomness test from "Some difficult-to-pass tests of randomness" by George Marsaglia and Wai Wan
 * Tsang
 */
public class BirthdaySpacings {

  private final Supplier<BigInteger> rng;
  private final BigInteger n;

  public BirthdaySpacings(Supplier<BigInteger> rng, BigInteger daysInYear) {
    this.rng = rng;
    this.n = daysInYear;
  }

  /**
   * Test the p-value for the Birthday spacings test applied with <i>nBirthdays</i> birthdays drawn
   * from the RNG in the interval [0, n) <i>observations</i> times. The result should be interpreted
   * as testing the null hypothesis that the birthdays are distributed randomly, so a small p value
   * (< 0.05) should indicate that the RNG is <b>not</b> random.
   *
   * @link https://www.researchgate.net/publication/5142801_Some_Difficult-to-Pass_Tests_of_Randomness/link/0e5fcf01f0c404bcbfaaa832/download
   */
  public double test(int nBirthdays, int observations) {

    int[] observed = new int[observations];
    double lambda = BigDecimal
        .valueOf(nBirthdays).pow(3)
        .divide(new BigDecimal(n.multiply(BigInteger.valueOf(4))), RoundingMode.HALF_UP)
        .doubleValue();

    // For each k, we compute the number of duplicates among the spacings of the
    // random values. These are supposed to be Poisson distributed with mean lambda, and we get the p-value
    // as the Chi squared test of the hypothesis that this is the case.
    for (int k = 0; k < observations; k++) {
      List<BigInteger> birthdays = Stream.generate(rng).limit(nBirthdays).sorted().collect(Collectors.toList());

      BigInteger[] d = new BigInteger[nBirthdays - 1];
      for (int i = 1; i < nBirthdays; i++) {
        d[i - 1] = birthdays.get(i).subtract(birthdays.get(i - 1));
      }

      int duplicates = 0;
      Arrays.sort(d);
      for (int i = 1; i < d.length; i++) {
        if (d[i - 1].equals(d[i])) {
          duplicates++;
          while (i < d.length - 1 && d[i + 1].equals(d[i])) {
            i++;
          }
        }
      }
      observed[k] = duplicates;
    }

    PoissonDistribution expectedDistribution = new PoissonDistribution(lambda);
    ChiSquareTest test = new ChiSquareTest();

    int max = Arrays.stream(observed).max().getAsInt();
    long[] data = new long[max];
    double[] expected = new double[max];
    for (int i = 0; i < max; i++) {
      int finalI = i;
      data[i] = Arrays.stream(observed).filter(j -> j == finalI).count();
      expected[i] = expectedDistribution.probability(i) * observations;
    }

    return test.chiSquareTest(expected, data);
  }

}