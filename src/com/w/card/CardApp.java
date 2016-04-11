package com.w.card;

import com.w.card.domain.ui.CMDUserInterface;
import com.w.card.domain.ui.UserInterface;
import com.w.card.store.JavaSerializationStore;
import com.w.card.store.Store;

public class CardApp {

	public static void main(String[] args) throws Exception {
		final Store store = new JavaSerializationStore("ICBCCard.txt");
		final UserInterface ui = new CMDUserInterface();
		ui.show(store);
	}

}
