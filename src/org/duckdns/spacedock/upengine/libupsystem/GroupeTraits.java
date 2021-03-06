/*
 * Copyright (C) 2017 ykonoclast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.duckdns.spacedock.upengine.libupsystem;

import java.util.EnumMap;
import org.duckdns.spacedock.commonutils.ErrorHandler;
import org.duckdns.spacedock.commonutils.PropertiesHandler;
import org.duckdns.spacedock.upengine.libupsystem.RollGenerator.RollResult;

/**
 *
 * @author ykonoclast
 */
public class GroupeTraits
{

    /**
     * l'ensebmle des valeurs numériques correspondant aux rangs des traits
     */
    private final EnumMap<Trait, Integer> m_traits;

    /**
     * constructeur prenant les divers rangs de traits en paramétre
     *
     * @param p_physique
     * @param p_coordination
     * @param p_mental
     * @param p_volonte
     * @param p_presence
     */
    public GroupeTraits(int p_physique, int p_coordination, int p_mental, int p_volonte, int p_presence)
    {
	m_traits = new EnumMap<>(Trait.class);
	setTrait(Trait.PHYSIQUE, p_physique);
	setTrait(Trait.COORDINATION, p_coordination);
	setTrait(Trait.MENTAL, p_mental);
	setTrait(Trait.VOLONTE, p_volonte);
	setTrait(Trait.PRESENCE, p_presence);
    }

    /**
     * effectue le jet de trait
     *
     * @param p_idTrait
     * @param p_ND
     * @param p_isSonne
     * @return
     */
    RollResult effectuerJetTrait(Trait p_idTrait, int p_ND, boolean p_isSonne)
    {
	return RollGenerator.getInstance().effectuerJet(p_ND, m_traits.get(p_idTrait), m_traits.get(p_idTrait), p_isSonne, 0);
    }

    /**
     * met à jour le trait visé par l'identifiant en paramétre
     *
     * @param p_idTrait
     * @param p_rang
     */
    void setTrait(Trait p_idTrait, int p_rang)
    {
	if (p_rang >= 0 && p_rang < 11)
	{
	    m_traits.put(p_idTrait, p_rang);
	}
	else
	{
	    ErrorHandler.paramAberrant(PropertiesHandler.getInstance("libupsystem").getString("trait") + ":" + p_rang);
	}
    }

    /**
     *
     * @param p_idTrait
     * @return le rang du trait identifié par l'identifiant passé en paramétre
     */
    int getTrait(Trait p_idTrait)
    {
	return m_traits.get(p_idTrait);
    }

    /**
     * enum contenant les différents traits possibles
     */
    public enum Trait
    {
	PHYSIQUE, COORDINATION, VOLONTE, MENTAL, PRESENCE
    }
}
