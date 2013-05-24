package edu.umich.insoar.language;

import java.util.Map;

import sml.Agent;
import sml.Identifier;

public abstract class LinguisticEntity {
	public abstract void extractLinguisticComponents(String string, Map tagsToWords);
	public abstract void translateToSoarSpeak(Identifier id, String connectingString);
}
