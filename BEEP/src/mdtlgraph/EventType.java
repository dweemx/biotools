package mdtlgraph;
public enum EventType {
	D, // Duplication
	DD,
	S, // Speciation
	T, // Transfer
	TTD, // Transfer To Dead
	TFD, // Transfer From Dead
	SL, // Speciation Loss
	TL, // Transfer Loss
	TLTD, // Transfer Loss to Unsampled/Extinct Species
	TLFD // Transfer From Unsampled/Extinct Species
}