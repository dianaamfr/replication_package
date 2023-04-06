package com.dissertation.referencearchitecture.compute.service;

import com.dissertation.WriteRequest;
import com.dissertation.WriteResponse;
import com.dissertation.WriteServiceGrpc.WriteServiceImplBase;

import io.grpc.stub.StreamObserver;

public class WriteServiceImpl extends WriteServiceImplBase {
    @Override
    public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
     WriteResponse response = WriteResponse.newBuilder().build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
}
