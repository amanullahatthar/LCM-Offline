package com.wood2.league;

import java.util.*;
import java.io.*;
import java.math.*;

enum ActionType {
	PASS, PICK, SUMMON, ATTACK
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
}

class Participant {
	int hp;
	int mana;
	int deck;
	int rune;
	int draw;
}

class Card {
	int cardNumber;
	int instanceId;
	CardLocation location;
	int cardType;
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
		default:
			action += "PASS";
			break;
		}
		if (action.equals("")) {
			System.err.println("Action not created...");
			System.exit(0);
		}
		action += ";";
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
	Participant participants[] = new Participant[2];
	int opponentHand;
	List<Card> cards = new ArrayList<Card>();

	boolean isDraftPhase() {
		return participants[0].mana == 0;
	}
}

class Bot {
	State state = new State();
	Turn bestTurn = new Turn();
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
			card.cardType = in.nextInt();
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

	void think() {
		bestTurn.clear();
		int myMana = state.participants[0].mana;

		if (state.isDraftPhase()) {
			return;
		}

		// Battle Phase
		int maxAttack = Integer.MIN_VALUE;
		Card bestCard = null;
		for (Card card : state.cards) {
			if (card.location != CardLocation.inHand)
				continue;
			if (card.cost > myMana)
				continue;
			if (card.attack > maxAttack) {
				maxAttack = card.attack;
				bestCard = card;
			}
		}
		if (null != bestCard) {
//			System.err.println("Found best card");
			Action a = new Action();
			a.summon(bestCard.instanceId);
			bestTurn.actions.add(a);
		}

		List<Card> enemyGuards = new ArrayList<Card>();
		for (Card eachCard : state.cards) {
			if (eachCard.location != CardLocation.enemyBoard)
				continue;
			if (!eachCard.guard)
				continue;
			enemyGuards.add(eachCard);
		}

		Action a;
		for (Card myCard : state.cards) {
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
}

class Player {

	public static void main(String args[]) {
		Bot bot = new Bot();
		// game loop
		while (true) {
			bot.read();
			bot.think();
			bot.print();
		}
	}
}
