/**
 *  Copyright 2003-2007 Greg Luck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eel.kitchen.pam;

import net.sf.jpam.PamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PamService
{
    private static final Logger logger
        = LoggerFactory.getLogger(PamService.class);

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
