/*
 *
 *  *  Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://orientdb.com
 *
 */
package com.orientechnologies.orient.client.remote.message;

import java.io.IOException;

import com.orientechnologies.orient.client.binary.OChannelBinaryAsynchClient;
import com.orientechnologies.orient.client.remote.OBinaryRequest;
import com.orientechnologies.orient.client.remote.OStorageRemoteSession;
import com.orientechnologies.orient.core.storage.OPhysicalPosition;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinary;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinaryProtocol;

public class OCeilingPhysicalPositionsRequest implements OBinaryRequest<OCeilingPhysicalPositionsResponse> {
  private int               clusterId;
  private OPhysicalPosition physicalPosition;

  public OCeilingPhysicalPositionsRequest(int clusterId, OPhysicalPosition physicalPosition) {
    this.clusterId = clusterId;
    this.physicalPosition = physicalPosition;
  }

  public OCeilingPhysicalPositionsRequest() {
  }

  @Override
  public void write(OChannelBinaryAsynchClient network, OStorageRemoteSession session) throws IOException {
    network.writeInt(clusterId);
    network.writeLong(physicalPosition.clusterPosition);
  }

  public void read(OChannelBinary channel, int protocolVersion, String serializerName) throws IOException {
    this.clusterId = channel.readInt();
    this.physicalPosition = new OPhysicalPosition(channel.readLong());
  }

  public OPhysicalPosition getPhysicalPosition() {
    return physicalPosition;
  }

  @Override
  public byte getCommand() {
    return OChannelBinaryProtocol.REQUEST_POSITIONS_CEILING;
  }

  public int getClusterId() {
    return clusterId;
  }

  @Override
  public OCeilingPhysicalPositionsResponse createResponse() {
    return new OCeilingPhysicalPositionsResponse();
  }

}