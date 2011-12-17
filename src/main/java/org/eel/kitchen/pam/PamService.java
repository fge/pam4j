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

public final class PamService
{
    private static final Logger logger
        = LoggerFactory.getLogger(PamService.class);

    private static final String SONAME = "jpam";

    public static final PamService DEFAULT_SERVICE;

    static {
        System.loadLibrary(SONAME);
        try {
            DEFAULT_SERVICE = new PamService("net-sf-" + SONAME);
        } catch (PamException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final String service;

    public PamService(final String service)
        throws PamException
    {
        if (service == null)
            throw new PamException("service name is null");
        if (service.isEmpty())
            throw new PamException("service name is empty");

        logger.debug("Getting new handle for service {}", service);
        this.service = service;
    }

    public PamHandle getHandle(final String user, final String passwd)
        throws PamException
    {
        return new PamHandle(service, user, passwd);
    }
}
