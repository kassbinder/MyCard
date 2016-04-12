package com.w.card;

import com.w.card.store.JavaSerializationStore;
import com.w.card.store.Store;
import com.w.card.ui.CMDFluntUserInterface;
import com.w.card.ui.UserInterface;

public class CardApp {

	public static void main(String[] args) throws Exception {
		final Store store = new JavaSerializationStore("ICBCCard.txt");
		final UserInterface ui = new CMDFluntUserInterface();
		ui.show(store);
	}

}

