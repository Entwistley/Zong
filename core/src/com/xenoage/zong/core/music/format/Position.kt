package com.xenoage.zong.core.music.format

import com.xenoage.utils.Mm
import com.xenoage.zong.core.music.LP


/**
 * Custom positioning of an object.
 */
class Position(
	/** The x coordinate in mm, or null for default. */
	val x: Mm? = null,
	/** The y coordinate in LP, or null for default. */
	val y: LP? = null,
	/** An additional horizontal offset in mm, or null for 0. */
	val relativeXOrNull: Mm? = null,
	/** An additional vertical offset in LP, or null for 0. */
	val relativeYOrNull: LP? = null
) : Positioning {

	companion object {

		/**
		 * The additional horizontal offset of this [Position],
		 * or 0 if this position is null or the offset is null.
		 */
		val Position?.relativeX: Float
			get() = if (this == null || relativeX == null) 0f else relativeX

		/**
		 * The additional vertical offset of this [Position],
		 * or 0 if this position is null or the offset is null.
		 */
		val Position?.relativeY: Float
			get() = if (this == null || relativeY == null) 0f else relativeY
	}

}
