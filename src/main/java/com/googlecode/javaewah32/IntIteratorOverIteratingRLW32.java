package com.googlecode.javaewah32;

import static com.googlecode.javaewah.EWAHCompressedBitmap.wordinbits;

import com.googlecode.javaewah.IntIterator;

/*
 * Copyright 2009-2013, Daniel Lemire, Cliff Moon, David McIntosh, Robert Becho, Google Inc., Veronika Zenz and Owen Kaser
 * Licensed under APL 2.0.
 */
/**
 * Implementation of an IntIterator over an IteratingRLW.
 * 
 *
 */
public class IntIteratorOverIteratingRLW32 implements IntIterator {
	IteratingRLW32 parent;
	private int position;
	private int runningLength;
	private long word;
	private int wordPosition;
	private int wordLength;
	private int literalPosition;
	private boolean hasnext;

	/**
	 * @param p iterator we wish to iterate over
	 */
	public IntIteratorOverIteratingRLW32(final IteratingRLW32 p) {
		this.parent = p;
		this.position = 0;

	}

	/**
	 * @return whether we could find another set bit
	 */
	private final boolean moveToNext() {
		while (!runningHasNext() && !literalHasNext()) {
			if (!eatRunningLengthWord())
				return false;
		}
		return true;
	}

	@Override
	public boolean hasNext() {
		return this.hasnext;
	}

	@Override
	public final int next() {
		final int answer;
		if (runningHasNext()) {
			answer = this.position++;
		} else {
			final int bit = Long.numberOfTrailingZeros(this.word);
			this.word ^= (1l << bit);
			answer = this.literalPosition + bit;
		}
		this.hasnext = this.moveToNext();
		return answer;
	}

	private final boolean eatRunningLengthWord() {
		this.runningLength = wordinbits * this.parent.getRunningLength()
				+ this.position;
		if (!this.parent.getRunningBit()) {
			this.position = this.runningLength;
		}
		this.wordPosition = this.parent.getNumberOfLiteralWords();
		this.wordLength = this.wordPosition
				+ this.parent.getNumberOfLiteralWords();
		return this.parent.next();
	}

	private final boolean runningHasNext() {
		return this.position < this.runningLength;
	}

	private final boolean literalHasNext() {
		while (this.word == 0 && this.wordPosition < this.wordLength) {
			this.word = this.parent.getLiteralWordAt(this.wordPosition++);
			this.literalPosition = this.position;
			this.position += wordinbits;
		}
		return this.word != 0;
	}
}
