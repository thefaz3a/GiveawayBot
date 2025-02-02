/*
 * Copyright 2017 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.giveawaybot.database.managers;

import com.jagrosh.easysql.DataManager;
import com.jagrosh.easysql.SQLColumn;
import com.jagrosh.easysql.columns.*;
import com.jagrosh.giveawaybot.Constants;
import com.jagrosh.giveawaybot.database.Database;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class GuildSettingsManager extends DataManager 
{
    public final static SQLColumn<Long> GUILD_ID = new LongColumn("GUILD_ID", false, 0, true);
    public final static SQLColumn<Integer> COLOR = new IntegerColumn("COLOR", false, Constants.BLURPLE.getRGB());
    public final static SQLColumn<Long> DEFAULT_CHANNEL = new LongColumn("DEFAULT_CHANNEL", false, 0); // currently unused
    public final static SQLColumn<Long> MANAGER_ROLE = new LongColumn("MANAGER_ROLE", false, 0L); // currently unused
    public final static SQLColumn<String> EMOJI = new StringColumn("EMOJI", true, null, 60); // currently unused
    
    public GuildSettingsManager(Database connector)
    {
        super(connector, "GUILD_SETTINGS");
    }
    
    public void updateColor(Guild guild)
    {
        int color = guild.getSelfMember().getColor()==null ? Constants.BLURPLE.getRGB() : guild.getSelfMember().getColor().getRGB();
        readWrite(selectAll(GUILD_ID.is(guild.getIdLong())), results -> 
        {
            if(results.next())
            {
                COLOR.updateValue(results, color);
                results.updateRow();
            }
            else
            {
                results.moveToInsertRow();
                GUILD_ID.updateValue(results, guild.getIdLong());
                COLOR.updateValue(results, color);
                results.insertRow();
            }
        });
    }
    
    public GuildSettings getSettings(long guildid)
    {
        return read(selectAll(GUILD_ID.is(guildid)), 
                results -> results.next() ? new GuildSettings(results) : new GuildSettings());
    }
    
    public class GuildSettings 
    {
        public final Color color;
        private final long defaultChannel;
        private final long managerRole;
        public final String emoji;
        
        private GuildSettings(int color, long defaultChannel, long managerRole, String emoji)
        {
            this.color = new Color(color);
            this.defaultChannel = defaultChannel;
            this.managerRole = managerRole;
            this.emoji = emoji == null ? Constants.TADA : emoji;
        }
        
        private GuildSettings()
        {
            this(Constants.BLURPLE.getRGB(), 0, 0, null);
        }
        
        private GuildSettings(ResultSet rs) throws SQLException
        {
            this(COLOR.getValue(rs), DEFAULT_CHANNEL.getValue(rs), MANAGER_ROLE.getValue(rs), EMOJI.getValue(rs));
        }
        
        public String getEmojiDisplay()
        {
            return emoji;
        }
        
        public TextChannel getDefaultChannel(Guild guild)
        {
            return guild.getTextChannelById(defaultChannel);
        }
        
        public Role getManagerRole(Guild guild)
        {
            return guild.getRoleById(managerRole);
        }
    }
}
