package api.units;

public enum UnitRarity {
	COMMON(0), UNCOMMON(1), RARE(2), LEGENDARY(3);

	public final int rank;
	
	private UnitRarity(int rank) {
		this.rank = rank;
	}
}