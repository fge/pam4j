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

import net.sf.jpam.PamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PamHandle
{
    private static final Logger logger
        = LoggerFactory.getLogger(PamHandle.class);

    private final String service;
    private final String user;
    private final String passwd;

    PamHandle(final String service, final String user, final String passwd)
        throws PamException
    {
        this.service = service;

        if (user == null)
            throw new PamException("user name is null");

        this.user = user;

        if (passwd == null)
            throw new PamException("credentials are null");

        this.passwd = passwd;
    }

    public synchronized PamReturnValue authenticate()
        throws PamException
    {
        final int id = authenticate(service, user, passwd,
            logger.isDebugEnabled());
        return PamReturnValue.fromId(id);
    }

    private native int authenticate(final String service, final String user,
        final String passwd, final boolean debug);
}
