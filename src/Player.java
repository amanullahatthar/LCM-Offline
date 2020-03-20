import java.util.*;
import java.io.*;
import java.math.*;

enum ActionType {
	PASS, PICK, SUMMON, ATTACK, USE
}

enum CardLocation {
	inHand(0), myBoard(1), enemyBoard(-1);

	// declaring private variable for getting values
	private int value;

	// getter method
	public int getAction() {
		return value;
	}

	// enum constructor - cannot be public or protected
	private CardLocation(int location) {
		value = location;
	}

	public static CardLocation getLocation(int value) {
		for (CardLocation loc : CardLocation.values()) {
			if (value == loc.value) {
				return loc;
			}
		}
		return null;
	}

//	public static int getValue(CardLocation location) {
//		for(CardLocation loc: CardLocation.values()) {
//			if(location == loc.)
//		}
//	}
}

enum CardType {
	creature(0), greenItem(1), redItem(2), blueItem(3);

	private int value;

	public int getAcion() {
		return value;
	}

	private CardType(int type) {
		value = type;
	}

	public static CardType getType(int value) {
		for (CardType type : CardType.values()) {
			if (value == type.value) {
				return type;
			}
		}
		return null;
	}
}

class BitStream {
	final int MAX_CAPACITY = 512;

	char[] buffer = new char[MAX_CAPACITY];

	char[] encodingTable = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".toCharArray();
	Map<Character, Integer> decodingTable = new HashMap<Character, Integer>();
	boolean decodingTableInit = false;

	int iter = 0;
	int bitCount = 0;

	public BitStream() {
		if (decodingTableInit)
			return;
		for (int i = 0; i < 64; i++) {
			decodingTable.put(encodingTable[i], i);
		}
		decodingTableInit = true;
	}

	void incBitCount() {
		bitCount++;
		if (bitCount >= 6) {
			bitCount = 0;
			iter++;
			if (iter >= MAX_CAPACITY) {
				System.out.println("BitStream buffer is full");
				System.exit(0);
			}
		}
	}

	int readBit() {
		int bit = buffer[iter] & (1 << (5 - bitCount));
		incBitCount();
		return bit > 0 ? 1 : 0;
	}

	void writeBit(boolean value) {
		buffer[iter] <<= 1;
		if (value) {
			buffer[iter] |= 1;
		}
		incBitCount();
	}

	void encode() {
		while (bitCount != 0)
			writeBit(false);
		for (int i = 0; i < iter; i++) {
			buffer[i] = encodingTable[buffer[i]];
		}
	}

	void decode(int count) {
		for (int i = 0; i < count; i++) {
			int value = decodingTable.get(buffer[i]);
			buffer[i] = (char) value;
		}
	}

	void print() {
		System.err.print("Bit stream: ");
		System.err.println(buffer);
	}

	void initRead(String str) {
		buffer = str.toCharArray();
		decode(str.length());
		iter = bitCount = 0;
	}

	int readInt(int bits) {
		int negative = readBit();
		int result = 0;
		for (int i = 0; i < bits; i++) {
			result <<= 1;
			int bit = readBit();
			if (bit > 0)
				result |= 1;
		}
		return negative > 0 ? -result : result;
	}

	void writeInt(int value, int bits) {
		writeBit(value < 0);
		value = Math.abs(value);
		int mask = 1 << (bits - 1);
		for (int i = 0; i < bits; i++) {
			writeBit((value & mask) > 0 ? true : false);
			mask >>= 1;
		}
	}
}

class Participant {
	int hp;
	int mana;
	int deck;
	int rune;
	int draw;

	void read(BitStream bs) {
		hp = bs.readInt(7);
		mana = bs.readInt(4);
		deck = bs.readInt(6);
		rune = bs.readInt(3);
		draw = bs.readInt(3);
	}

	void write(BitStream bs) {
		bs.writeInt(hp, 7);
		bs.writeInt(mana, 4);
		bs.writeInt(deck, 6);
		bs.writeInt(rune, 3);
		bs.writeInt(draw, 3);
	}
}

class Card {
	int cardNumber;
	int instanceId;
	CardLocation location;
	CardType cardType;
	int cost;
	int attack;
	int defense;
	String abilities;
	int myHealthChange;
	int enemyHealthChange;
	int cardDraw;
	boolean breakthrough;
	boolean charge;
	boolean guard;

	boolean used;

	void read(BitStream bs) {
		cardNumber = bs.readInt(8);
		instanceId = bs.readInt(2);
		location = CardLocation.getLocation(bs.readInt(2));
		cardType = CardType.getType(bs.readInt(2));
		cost = bs.readInt(4);
		attack = bs.readInt(4);
		defense = bs.readInt(4);
		myHealthChange = bs.readInt(4);
		enemyHealthChange = bs.readInt(4);
		cardDraw = bs.readInt(3);
		breakthrough = bs.readBit() > 0 ? true : false;
		charge = bs.readBit() > 0 ? true : false;
		guard = bs.readBit() > 0 ? true : false;
		used = false;
	}

	void write(BitStream bs) {
		bs.writeInt(cardNumber, 8);
		bs.writeInt(instanceId, 2);
		bs.writeInt(location.getAction(), 2);
		bs.writeInt(cardType.getAcion(), 2);
		bs.writeInt(cost, 4);
		bs.writeInt(attack, 4);
		bs.writeInt(defense, 4);
		bs.writeInt(myHealthChange, 4);
		bs.writeInt(enemyHealthChange, 4);
		bs.writeInt(cardDraw, 3);
		bs.writeBit(breakthrough);
		bs.writeBit(charge);
		bs.writeBit(guard);
	}
}

class Action {
	ActionType type = ActionType.PASS;
	int id = -1;
	int targetId = -1;

	void pass() {
		type = ActionType.PASS;
	}

	void pick(int _id) {
		type = ActionType.PICK;
		id = _id;
	}

	void summon(int _id) {
		type = ActionType.SUMMON;
		id = _id;
	}

	void attack(int _id, int _targetId) {
		type = ActionType.ATTACK;
		id = _id;
		targetId = _targetId;
	}

	void use(int _id, int _targetId) {
		type = ActionType.USE;
		id = _id;
		targetId = _targetId;
	}

	String print() {
		String action = "";
		switch (type) {
		case PICK:
			action += "PICK " + id;
			break;
		case SUMMON:
			action += "SUMMON " + id;
			break;
		case ATTACK:
			action += "ATTACK " + id + " " + targetId;
			break;
		case USE:
			action += "USE " + id + " " + targetId;
			break;
		default:
			action += "PASS";
			break;
		}
		if (action.equals("")) {
			System.err.println("Action not created...");
			System.exit(0);
		}
		action += type != ActionType.PICK ? ";" : "";
		return action;
	}
}

class Turn {
	List<Action> actions = new ArrayList<Action>();

	void print() {
		if (actions.size() == 0) {
			System.out.println("PASS");
			return;
		}
		String commands = "";
		for (Action action : actions) {
//			System.err.println(commands);
			commands += action.print();
		}
		System.out.println(commands);
	}

	void clear() {
		actions.clear();
	}
}

class State {
	Participant participants[] = { new Participant(), new Participant() };
	int opponentHand;
	List<Card> cards = new ArrayList<Card>();

	boolean isDraftPhase() {
		return participants[0].mana == 0;
	}

	void read(BitStream bs) {
		for (int i = 0; i < 2; i++)
			participants[i].read(bs);
		opponentHand = bs.readInt(3);
		int cardCount = bs.readInt(8);
		cards.clear();
		Card card;
		for (int i = 0; i < cardCount; i++) {
			card = new Card();
			card.read(bs);
			cards.add(card);
		}
	}

	void write(BitStream bs) {
		for (int i = 0; i < 2; i++)
			participants[i].write(bs);
		bs.writeInt(opponentHand, 3);
		bs.writeInt(cards.size(), 8);
		for (Card card : cards)
			card.write(bs);
	}
}

class ManaCurve {
	int[] curve = new int[Player.MAX_MANA_COST + 1];

	void compute(List<Card> draftedCards) {
		Arrays.fill(curve, 0);
		for (Card card : draftedCards) {
			curve[card.cost]++;
		}
	}

	int evalScore() {
		int low = 0, medium = 0, high = 0;
		for (int i = 0; i <= 2; i++)
			low += curve[i];
		for (int i = 3; i <= 5; i++)
			medium += curve[i];
		for (int i = 6; i <= 8/* Player.MAX_MANA_COST */; i++)
			high += curve[i];

		return Math.abs(low - 15) + Math.abs(medium - 10) + Math.abs(high - 5);
	}

	void print() {
		for (int i = 0; i <= Player.MAX_MANA_COST; i++) {
			System.err.println(i + " : " + curve[i] + " cards");
		}
	}
}

class Bot {
	State state = new State();
	Turn bestTurn = new Turn();
	List<Card> draftedCards = new ArrayList<Card>();
	Scanner in = new Scanner(System.in);

	void read() {
		state.cards.clear();
		Participant p;
		for (int i = 0; i < 2; i++) {
			p = new Participant();
			p.hp = in.nextInt();
			p.mana = in.nextInt();
			p.deck = in.nextInt();
			p.rune = in.nextInt();
			p.draw = in.nextInt();
			state.participants[i] = p;
		}
		int opponentHand = in.nextInt();
		state.opponentHand = opponentHand;

		int opponentActions = in.nextInt();
		if (in.hasNextLine()) {
			in.nextLine();
		}
		for (int i = 0; i < opponentActions; i++) {
			String cardNumberAndAction = in.nextLine();
		}

		int cardCount = in.nextInt();
		Card card;
		for (int i = 0; i < cardCount; i++) {
			card = new Card();
			card.cardNumber = in.nextInt();
			card.instanceId = in.nextInt();
			card.location = CardLocation.getLocation(in.nextInt());
//			int  loc = in.nextInt();
//			card.location = CardLocation.getLocation(loc);
//			System.err.println("loc: " + loc + ",location:" + card.location);
			card.cardType = CardType.getType(in.nextInt());
			card.cost = in.nextInt();
			card.attack = in.nextInt();
			card.defense = in.nextInt();
			card.abilities = in.next();
			card.myHealthChange = in.nextInt();
			card.enemyHealthChange = in.nextInt();
			card.cardDraw = in.nextInt();
			for (char ability : card.abilities.toCharArray()) {
				if (ability == 'B')
					card.breakthrough = true;
				if (ability == 'C')
					card.charge = true;
				if (ability == 'G')
					card.guard = true;
			}
			state.cards.add(card);
		}
	}

	void print() {
		bestTurn.print();
	}

	Card[] findBestPair(List<Card> targets) {
		Card[] cardPair = {};
		int bestScore = Integer.MIN_VALUE;
		Card myBestCard = null, bestEnemyCard = null;
		for (Card myCard : state.cards) {
			if (myCard.used)
				continue;
			if (myCard.location != CardLocation.myBoard)
				continue;
			for (Card enemyCard : targets) {
				if (enemyCard.used)
					continue;
				if (enemyCard.location != CardLocation.enemyBoard)
					continue;
				int score = myCard.attack >= enemyCard.defense && myCard.defense > enemyCard.attack ? 100 : 0;
				if (score > bestScore) {
					bestScore = score;
					myBestCard = myCard;
					bestEnemyCard = enemyCard;
				}
			}
		}
		if (bestScore <= 0)
			return cardPair;
		System.err.println("Card match found!!!");
		cardPair = new Card[2];
		cardPair[0] = myBestCard;
		cardPair[1] = bestEnemyCard;
		return cardPair;
	}

	void think() {
		bestTurn.clear();
		int myMana = state.participants[0].mana;

		ManaCurve manaCurve = new ManaCurve();
		manaCurve.compute(draftedCards);
		if (state.isDraftPhase()) {
			int bestScore = Integer.MAX_VALUE, bestPick = -1;
			for (int i = 0; i < 3; i++) {
				Card card = state.cards.get(i);
				manaCurve.curve[card.cost]++;
				int score = manaCurve.evalScore();
				manaCurve.curve[card.cost]--;

				if (score < bestScore) {
					bestScore = score;
					bestPick = i;
				}
			}

			Action action = new Action();
			action.pick(bestPick);
			bestTurn.actions.add(action);
			draftedCards.add(state.cards.get(bestPick));

			return;
		}
//		manaCurve.print();

		// Battle Phase
		List<Card> myCreatures = new ArrayList<Card>();
		List<Card> enemyCreatures = new ArrayList<Card>();
		for (Card c : state.cards) {
			if (c.location == CardLocation.myBoard) {
				myCreatures.add(c);
			} else if (c.location == CardLocation.enemyBoard) {
				enemyCreatures.add(c);
			}
		}
		while (myMana > 0) {
			int maxAttack = Integer.MIN_VALUE;
			Card bestCard = null;
			for (Card card : state.cards) {
				if (card.location != CardLocation.inHand)
					continue;
				if (card.cardType == CardType.greenItem && myCreatures.size() == 0)
					continue;
				if (card.cardType == CardType.redItem && enemyCreatures.size() == 0)
					continue;
				if (card.cardType == CardType.blueItem)
					continue;
				if (card.cost > myMana)
					continue;
				if (card.attack > maxAttack) {
					maxAttack = card.attack;
					bestCard = card;
				}
			}
			if (null == bestCard)
				break;

//			System.err.println("Found best card");
			Action a = new Action();
			if (bestCard.cardType == CardType.creature) {
				a.summon(bestCard.instanceId);
				myCreatures.add(bestCard);
				bestCard.used = true;
			} else if (bestCard.cardType == CardType.greenItem) {
				a.use(bestCard.instanceId, myCreatures.get(0).instanceId);
			} else if (bestCard.cardType == CardType.redItem) {
				a.use(bestCard.instanceId, enemyCreatures.get(0).instanceId);
			}
			bestTurn.actions.add(a);
			myMana -= bestCard.cost;
			state.cards.remove(bestCard);
		}

		List<Card> enemyGuards = new ArrayList<Card>();
		List<Card> enemyTargets = new ArrayList<Card>();
		for (Card eachCard : state.cards) {
			if (eachCard.location != CardLocation.enemyBoard)
				continue;
			if (eachCard.guard)
				enemyGuards.add(eachCard);
			else
				enemyTargets.add(eachCard);
		}

		while (true) {
			if (enemyGuards.size() > 0)
				performAttack(enemyGuards);
			if (enemyGuards.size() > 0)
				break;

			if (enemyTargets.size() == 0)
				break;
			if (!performAttack(enemyTargets))
				break;
		}

//		if (enemyGuards.size() == 0) {
//			for (Card card : state.cards) {
//				if (card.location != CardLocation.myBoard)
//					continue;
//				if (card.used)
//					continue;
//				Action action = new Action();
//				action.attack(card.instanceId, -1);
//				bestTurn.actions.add(action);
//			}
//		}

		Action a;
		for (Card myCard : state.cards) {
			if (myCard.used)
				continue;
			if (myCard.location != CardLocation.myBoard)
				continue;
			a = new Action();
			if (enemyGuards.size() == 0) {
				a.attack(myCard.instanceId, -1);
			} else {
				Card enemyGuard = enemyGuards.get(0);
				enemyGuard.defense -= myCard.attack;
				if (enemyGuard.defense <= 0)
					enemyGuards.remove(0);
				a.attack(myCard.instanceId, enemyGuard.instanceId);
			}
			bestTurn.actions.add(a);
		}
		System.err.println(bestTurn.actions.size());
	}

	boolean performAttack(List<Card> targets) {
		Card[] cardPair = findBestPair(targets);
		if (cardPair.length == 0)
			return false;
		Card myCard = cardPair[0], enemyCard = cardPair[1];
		myCard.used = enemyCard.used = true;
		Action a = new Action();
		a.attack(myCard.instanceId, enemyCard.instanceId);
		bestTurn.actions.add(a);
		targets.remove(enemyCard);
		return true;
	}
}

class Player {

	public static int MAX_MANA_COST = 12;

	public static void main(String args[]) {
//		Remove
//		this code 
//		before submission
		if (System.getProperty("os.name").equals("Linux")
				&& System.getProperty("os.version").equals("4.15.0-88-generic")) {
			debug();
			return;
		}
//		Remove the
//		above code
//		before submission

		Bot bot = new Bot();
		// game loop
		while (true) {
			bot.read();

			BitStream bs = new BitStream();
			bot.state.write(bs);
			bs.encode();
			bs.print();

			bot.think();
			bot.print();
		}
	}

	public static void debug() {
		BitStream testBitStream = new BitStream();
		testBitStream.initRead("71bn4W4N4M0uXO12300GU22880001PW6EK002W8OW05800M04GGW06OW46C4G9W90XY002");
		State s = new State();
		s.read(testBitStream);

		Bot bot = new Bot();
		bot.state = s;
		bot.think();
		bot.print();
	}
}