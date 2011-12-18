/*
 * Copyright (c) 2011, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eel.kitchen.pam;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class PamServiceTest
{
    @Test
    public void testNullService()
    {
        try {
            new PamService(null);
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "service name is null");
        }
    }

    @Test
    public void testEmptyServiceName()
    {
        try {
            new PamService("");
            fail("No exception thrown");
        } catch (PamException e) {
            assertEquals(e.getMessage(), "service name is empty");
        }
    }
}
