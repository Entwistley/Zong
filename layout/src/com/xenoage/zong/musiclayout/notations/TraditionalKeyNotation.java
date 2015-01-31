package com.xenoage.zong.musiclayout.notations;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.xenoage.utils.annotations.Const;
import com.xenoage.zong.core.music.key.TraditionalKey;
import com.xenoage.zong.core.music.time.Time;
import com.xenoage.zong.musiclayout.spacing.horizontal.ElementWidth;

/**
 * This class contains layout information
 * about a traditional key signature.
 *
 * @author Andreas Wenger
 */
@Const @AllArgsConstructor
public final class TraditionalKeyNotation
	implements Notation {

	@Getter public final TraditionalKey element;
	@Getter public final ElementWidth width;
	public final int linePositionC4;
	public final int linePositionMin;

}
