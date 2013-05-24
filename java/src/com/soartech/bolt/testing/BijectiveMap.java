package com.soartech.bolt.testing;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author chris
 *
 * @param <K1>
 * @param <K2>
 */
public class BijectiveMap<K1, K2> {
	private Map<K1, K2> leftMap = new HashMap<K1, K2>();
	private Map<K2, K1> rightMap = new HashMap<K2, K1>();
	
	public BijectiveMap() {
	}
	
	public void add(K1 k1, K2 k2) {
		if(!leftMap.containsKey(k1) && !rightMap.containsKey(k2)) {
			leftMap.put(k1, k2);
			rightMap.put(k2, k1);
		} else {
			throw new MapAlreadyContainsKeyException("One or both of the keys is already in the map.");
		}
	}
	
	/**
	 * 
	 * @param k1
	 * @return <code>null</code> if key is not present, the corresponding <code>K2</code> otherwise
	 */
	public K2 getLeft(K1 k1) {
		return leftMap.get(k1);
	}
	
	/**
	 * 
	 * @param k2
	 * @return <code>null</code> if key is not present, the corresponding <code>K1</code> otherwise
	 */
	public K1 getRight(K2 k2) {
		return rightMap.get(k2);
	}
}
