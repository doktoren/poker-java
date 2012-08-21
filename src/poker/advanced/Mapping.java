package poker.advanced;

import poker.representation.ParseException;
import poker.representation.PokerMisc;

public class Mapping {

	// mapIndex 0 is the identity mapping
	
	// suitMap[suit][mapIndex] -> suit'
	public static final int[][] suitMap = new int[4][1*2*3*4];

	// cardMap[card][mapIndex] -> card'
	public static final int[][] cardMap = new int[4*13][1*2*3*4];
	
	// cmop[a][b] Returns the index of the mapping b o a 
	public static final int[][] comp = new int[1*2*3*4][1*2*3*4];
	
	public static boolean print = false;
	
	public static final String mapIndexToString(int mapIndex) {
		StringBuffer sb = new StringBuffer("mapIndex "+mapIndex+" = (");
		for (int suit=0; suit<4; suit++) {
			if (suit != 0)
				sb.append(", ");
			sb.append(PokerMisc.SUIT_CHAR[suit]);
			sb.append("->");
			sb.append(PokerMisc.SUIT_CHAR[suitMap[suit][mapIndex]]);
		}
		return sb.toString()+")";
	}
	
	/**
	 * Modifies and returns cards.
	 */
	public static final int[] applyMapping(int[] cards, int mapIndex) {
		for (int i=0; i<cards.length; i++)
			cards[i] = cardMap[cards[i]][mapIndex];
		return cards;
	}
	
	/**
	 * Modifies and returns cards.
	 */
	public static final int[] applyMapping(int[] cards) {
		return applyMapping(cards, getMapping(cards));
	}
	
	/**
	 * Returns the map index that will bring the cards to their standard representation.
	 */
	public static final int getMapping(int[] cards) {
		int[] fixings = new int[13];
		for (int i=0; i<cards.length; i++)
			fixings[cards[i]>>2] += 1 << (cards[i]&3);
		
		int pmIndex=0;
		for (int rank=0; rank<13; rank++)
			pmIndex = partialMap[pmIndex][fixings[rank]];
				
		return pmToFullMap[pmIndex];
	}
	
	// #################################################################
	// ###########           INTERFACE STOPS HERE          #############
	// ################################################################# 
	

	// pmCompactIndex/pmFullIndex 0 is the open mapping (nothing has been decided yet).
	
	private static final int[] pmToCompactIndex = new int[256];
	private static final int[] pmToFullIndex;
	private static final int[][] partialMap; // partialMap[compact index][fixing]
	private static final int[] pmToFullMap;
	
	static {
		// Init suitMap
		for (int mapIndex=0; mapIndex<1*2*3*4; mapIndex++) {
			boolean[] taken = {false, false, false, false};
			int mi = mapIndex;
			for (int oldSuit=3; oldSuit>=0; oldSuit--) {
				int k = mi%(oldSuit+1);
				mi /= (oldSuit+1);
				int newSuit = -1;
				do {
					if (!taken[++newSuit])
						--k;
				} while (k>=0);
				taken[newSuit] = true;
				
				suitMap[oldSuit][23-mapIndex] = newSuit;
				if (print) System.out.println("suitMap["+oldSuit+"]["+mapIndex+"] = "+newSuit);
			}
		}
		
		// Validate suitMap
		for (int mi1=0; mi1<1*2*3*4; mi1++)
			for (int mi2=0; mi2<mi1; mi2++) {
				boolean equal = true;
				for (int i=0; i<4; i++)
					if (suitMap[i][mi1] != suitMap[i][mi2]) {
						equal = false;
						break;
					}
				assert !equal;
			}
		
		// Init comp
		for (int mia=0; mia<1*2*3*4; mia++)
			for (int mib=0; mib<1*2*3*4; mib++)
				for (int mi=0; ; mi++) {
					assert mi<24;
					if (suitMap[suitMap[0][mia]][mib] == suitMap[0][mi]  &&
							suitMap[suitMap[1][mia]][mib] == suitMap[1][mi]  &&
							suitMap[suitMap[2][mia]][mib] == suitMap[2][mi]  &&
							suitMap[suitMap[3][mia]][mib] == suitMap[3][mi]) {
						comp[mia][mib] = mi;
						break;
					}
				}
				
		
		// Init cardMap
		for (int card=0; card<52; card++)
			for (int mapIndex=0; mapIndex<24; mapIndex++)
				cardMap[card][mapIndex] = (card&~0x3) | suitMap[card&3][mapIndex];
		
		// Init partialMap...
		{
			// Init pmToCompactIndex
			int count = 0;
			for (int i=0; i<256; i++) {
				int a = i&3;
				int b = (i>>2)&3;
				int c = (i>>4)&3;
				int d = i>>6;
			
				//sort
				if (b<a) { int tmp=a; a=b; b=tmp; }
				if (d<c) { int tmp=c; c=d; d=tmp; }
				if (d<b) { int tmp=b; b=d; d=tmp; }
				if (c<a) { int tmp=a; a=c; c=tmp; }
				if (c<b) { int tmp=b; b=c; c=tmp; }
				
				boolean ok = a==0  &&  a+1>=b  &&  b+1>=c  && c+1>=d;
				if (print) System.out.println("("+a+","+b+","+c+","+d+") : "+ok);
				
				pmToCompactIndex[i] = ok ? count++ : -1;
			}
			
			// Init pmToFullIndex
			pmToFullIndex = new int[count];
			for (int i=0; i<256; i++)
				if (pmToCompactIndex[i] != -1)
					pmToFullIndex[pmToCompactIndex[i]] = i;
			
			// Init pmToFullMap
			pmToFullMap = new int[count];
			for (int pmCompactIndex=0; pmCompactIndex<count; pmCompactIndex++) {
				// First find mapping consistent with partial map.
				// It is consistent if f_i>f_j => p_i>=p_j
				int pmIndex = pmToFullIndex[pmCompactIndex];
				int[] p = new int[]{pmIndex & 3, (pmIndex>>2)&3, (pmIndex>>4)&3, pmIndex>>6};
				for (int mapIndex=0; ; mapIndex++) {
					assert mapIndex != 1*2*3*4;
					boolean match = true;
					
					for (int i=0; i<4 && match; i++)
						for (int j=0; j<4; j++)
							if (suitMap[i][mapIndex]>suitMap[j][mapIndex]  &&  p[i]<p[j]) {
								match = false;
								break;
							}
					
					if (match) {
						pmToFullMap[pmCompactIndex] = mapIndex;
						break;
					}
				}
			}
			
			// Init partialMap
			partialMap = new int[count][16];
			for (int pmCompactIndex=0; pmCompactIndex<count; pmCompactIndex++)
				for (int fixing=0; fixing<16; fixing++) {
					int pmIndex = pmToFullIndex[pmCompactIndex];
					
					// Example: (each digit of partialMap and result is 2 bits, fixing is 4*1 bits long)
					// pmIndex:	p = 0011
					// fixing:	f = 0110
					// result:	r = 1023
					
					// Post requirements (must hold for all i,j):
					// 	The ordering may not collapse:
					//		r_i = r_j => p_i = p_j
					//  The fixing has the desired effect:
					//		(f_i < f_j  &&  p_i = p_j) => r_i > r_j
					//  The ordering may not change:
					// 		p_i < p_j => r_i < r_j
					
					// Run through all 256 possible results and find the lowest first match
					// (which will be the right one, because it is lowest).
					
					int[] p = new int[]{pmIndex & 3, (pmIndex>>2)&3, (pmIndex>>4)&3, pmIndex>>6};
					int[] f = new int[]{fixing & 1, (fixing>>1)&1, (fixing>>2)&1, (fixing>>3)&1};
					
					for (int k=0; k<256; k++) {
						if (pmToCompactIndex[k] == -1)
							continue;
						
						int[] r = new int[]{k&3, (k>>2)&3, (k>>4)&3, (k>>6)&3};
						
						boolean match = true;
						for (int i=0; i<4  &&  match; i++)
							for (int j=0; j<4; j++)
								if ((r[i]==r[j]  &&  p[i]!=p[j])  ||
										(f[i]<f[j]  &&  p[i]==p[j]  &&  r[i]<=r[j])  ||
										(p[i]<p[j]  &&  r[i]>=r[j])) {
									match = false;
									//System.out.print("No: (i,j)=("+i+","+j+") : ");
									break;
								}
						
						if (match) {
							if (print)
								System.out.println("fixMapping({"+p[0]+","+p[1]+","+p[2]+","+p[3]+"}, {"+
										f[0]+","+f[1]+","+f[2]+","+f[3]+"}) = {"+r[0]+","+r[1]+","+r[2]+","+r[3]+"}"+
										".  toFullMap: "+pmToFullMap[pmToCompactIndex[k]]);

							partialMap[pmCompactIndex][fixing] = pmToCompactIndex[k];
							break;
						}
					}
				}
		}
		
		assert pmToFullIndex[0] == 0;
		assert pmToCompactIndex[0] == 0;
		assert pmToFullMap[0] == 0;
		//pmCompactIndex/pmFullIndex 0
	}

	

	
	public static final String test(String spaceSeparatedShortNames) throws ParseException {
		return spaceSeparatedShortNames + " -> " + PokerMisc.getShortNames(applyMapping(PokerMisc.getCardList(spaceSeparatedShortNames)));
	}
	
	public static void main(String[] args) throws ParseException {
		// Test
		if (print)
			for (int mapIndex=0; mapIndex<1*2*3*4; mapIndex++)
				System.out.println(mapIndexToString(mapIndex));
		
		System.out.println(test("Ks Ac 8s Tc 4h Js 6d"));
		System.out.println(test("9s 7c Ts 3h Jc Qh 3s"));
	}

}
