package com.xenoage.zong.commands.playback;

import static com.xenoage.zong.player.PlayerApplication.pApp;

import com.xenoage.zong.commands.Command;
import com.xenoage.zong.commands.CommandPerformer;


/**
 * This command starts the MIDI playback.
 * 
 * @author Andreas Wenger
 */
public class StartMidiPlaybackCommand
	extends Command
{

	@Override public void execute(CommandPerformer performer)
	{
		pApp().getPlayer().start();
	}

}
