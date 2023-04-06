package com.dissertation.referencearchitecture.compute.service;

import com.dissertation.ROTRequest;
import com.dissertation.ROTResponse;
import com.dissertation.ROTServiceGrpc.ROTServiceImplBase;

import io.grpc.stub.StreamObserver;

public class ROTServiceImpl extends ROTServiceImplBase {
    @Override
    public void rot(ROTRequest request, StreamObserver<ROTResponse> responseObserver) {
     ROTResponse response = ROTResponse.newBuilder().build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
}
