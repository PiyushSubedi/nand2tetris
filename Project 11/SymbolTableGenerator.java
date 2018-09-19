import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolTableGenerator {

	private List<SymbolTableEntry> classLevelEntries, subroutineLevelEntries;
	
	private List<SymbolTableEntry> statics;

	private HashMap<String, Integer> classIndexMap, subroutineIndexMap;
	

	public SymbolTableGenerator() {
		classLevelEntries = new ArrayList<>();
		classIndexMap = new HashMap<>();
		statics = new ArrayList<>();
		resetSubroutineTable();
	}
	
	public int getNumberOfClassLevelEntries() {
		return classLevelEntries.size() - statics.size();
	}
	
	public void resetSubroutineTable() {
		subroutineLevelEntries = new ArrayList<>();
		subroutineIndexMap = new HashMap<>();
	}

	public void addClassLevelEntry(String name, String type, String kind) {
		
		SymbolTableEntry entry = new SymbolTableEntry(name, type, kind, getNextIndexForKind(kind, true)); 		
		classLevelEntries.add(entry);
		
		if(kind.equals("static")) {
			statics.add(entry);
		}
	}
	
	public void addSubroutineEntry(String name, String type, String kind) {
		
		SymbolTableEntry entry = new SymbolTableEntry(name, type, kind, getNextIndexForKind(kind, false)); 
		subroutineLevelEntries.add(entry);
	}

	private int getNextIndexForKind(String kind, Boolean isClassLevelEntry) {

		HashMap<String, Integer> lookupMap = isClassLevelEntry ? classIndexMap : subroutineIndexMap;

		int nextIndex = 0;
		
		if(lookupMap.containsKey(kind)) {
			nextIndex = lookupMap.get(kind) + 1;
		}
		
		if(isClassLevelEntry) {
			classIndexMap.put(kind, nextIndex);		
		} else {
			subroutineIndexMap.put(kind, nextIndex);
		}

		return nextIndex;
	}
	
	public SymbolTableEntry lookup(String identifier) {

		for(SymbolTableEntry entry : subroutineLevelEntries) {
			if(entry.name.equals(identifier)) {
				return entry;
			}
		}
		
		for(SymbolTableEntry entry : classLevelEntries) {
			if(entry.name.equals(identifier)) {
				return entry;
			}
		}
		
		return null;
	}
		
	
	public final class SymbolTableEntry {
		
		private String name;
		
		private String type;
		
		private String kind;
		
		private int index;
		
		public SymbolTableEntry(String name, String type, String kind, int index) {
			this.name = name;
			this.type = type;
			this.kind = kind;
			this.index = index;
		}
		
		public String getType() {
			return type;
		}
	
		public String getSegment() {
			if(kind.equals("var")) {
				return "local";
			} else if(kind.equals("field")) {
				return "this";
			}
			
			return kind; // argument/static
		}
		
		public int getIndex() {
			return index;
		}
		
		
		@Override
		public String toString() {
			return name + "\t|\t" + type + "\t|\t" + kind + "\t|\t" + index;
		}
		
	}
}
