/*
 * Copyright  1999-2004 The Apache Software Foundation.
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
 *
 */
package org.apache.sandesha.storage.dao;

import org.apache.sandesha.Constants;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaDAOFactory {

    public static ISandeshaDAO getStorageAccessor(int accessor) {

        if (accessor == Constants.SERVER_QUEUE_ACCESSOR)
            return new SandeshaQueueDAO();
        else if (accessor == Constants.SERVER_DATABASE_ACCESSOR)
            return new SandeshaDatabaseDAO();
        else
            return null;

    }

}