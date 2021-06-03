/*
 * Copyright 2020 The Context Mapper Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mdsl.samples.grpc.reference_management;

import ReferenceManagementServiceAPI.PaperArchiveFacadeGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ReferenceManagementServiceAPI.ReferenceManagementAPI.PaperItemDTO;
import static ReferenceManagementServiceAPI.ReferenceManagementAPI.createPaperItemParameter;

public class ReferenceManagementClient {

    private static final Logger logger = Logger.getLogger(ReferenceManagementClient.class.getName());

    private final PaperArchiveFacadeGrpc.PaperArchiveFacadeBlockingStub blockingStub;

    public ReferenceManagementClient(Channel channel) {
        blockingStub = PaperArchiveFacadeGrpc.newBlockingStub(channel);
    }

    public void createPaperItem(String what, String who, String where) {
        logger.info("We will try to create a paper item with the values what='" + what + "', who='" + who + "', and where='" + where + "'.");
        createPaperItemParameter request = createPaperItemParameter.newBuilder()
                .setWhat(what)
                .setWho(who)
                .setWhere(where)
                .build();

        PaperItemDTO response;
        try {
            response = blockingStub.createPaperItem(request);
            logger.info("Received response: title=" + response.getTitle() + ", authors=" + response.getAuthors() + ", venue=" + response.getVenue());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        String target = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        try {
            ReferenceManagementClient client = new ReferenceManagementClient(channel);
            client.createPaperItem("Our super paper", "Stefan Kapferer und Olaf Zimmermann", "Ostschweizer Fachhochschule - OST");
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

}
