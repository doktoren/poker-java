package poker.util;

public class Math {
	
	public static final int[] LOG_TABLE = {
		  0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,
		  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
		  5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
		  5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
		  6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
		  6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
		  6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
		  6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7};
	
	// Contains a list of the first 53 primes numbers
	public static final int[] primes = new int[53];
	
	/**
	 * Calculated as if n was unsigned. intLog(0) gives 0.
	 */
	public static final int intLog(int n) {
		if ((n & 0xFFFFFF00) == 0)
			return LOG_TABLE[n & 0xFF];
		if ((n & 0xFFFF0000) == 0)
			return 8+LOG_TABLE[n >> 8];
		if ((n & 0xFF000000) == 0)
			return 16+LOG_TABLE[n >> 16];
		return 24+LOG_TABLE[(n >> 24) & 0xFF];
	}
	
	// #################################################################
	// ###########           INTERFACE STOPS HERE          #############
	// ################################################################# 
	
	static {
		// Init primes
		int index = 0;
		primes[index++] = 2;
		for (int n=3; index<primes.length; n+=2) {
			boolean isPrime = true;
			for (int j=0; primes[j]*primes[j]<=n; j++)
				if (n%primes[j] == 0) {
					isPrime = false;
					break;
				}
			if (isPrime)
				primes[index++] = n;
		}
	}
	
}
