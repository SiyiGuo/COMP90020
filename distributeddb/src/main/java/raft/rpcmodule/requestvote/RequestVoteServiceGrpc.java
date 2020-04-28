package raft.rpcmodule.requestvote;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 *声明一个服务名称
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.29.0)",
    comments = "Source: requestvote.proto")
public final class RequestVoteServiceGrpc {

  private RequestVoteServiceGrpc() {}

  public static final String SERVICE_NAME = "raft.RequestVoteService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<raft.rpcmodule.requestvote.RequestVoteRequest,
      raft.rpcmodule.requestvote.RequestVoteResponse> getRequestVoteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RequestVote",
      requestType = raft.rpcmodule.requestvote.RequestVoteRequest.class,
      responseType = raft.rpcmodule.requestvote.RequestVoteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<raft.rpcmodule.requestvote.RequestVoteRequest,
      raft.rpcmodule.requestvote.RequestVoteResponse> getRequestVoteMethod() {
    io.grpc.MethodDescriptor<raft.rpcmodule.requestvote.RequestVoteRequest, raft.rpcmodule.requestvote.RequestVoteResponse> getRequestVoteMethod;
    if ((getRequestVoteMethod = RequestVoteServiceGrpc.getRequestVoteMethod) == null) {
      synchronized (RequestVoteServiceGrpc.class) {
        if ((getRequestVoteMethod = RequestVoteServiceGrpc.getRequestVoteMethod) == null) {
          RequestVoteServiceGrpc.getRequestVoteMethod = getRequestVoteMethod =
              io.grpc.MethodDescriptor.<raft.rpcmodule.requestvote.RequestVoteRequest, raft.rpcmodule.requestvote.RequestVoteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RequestVote"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  raft.rpcmodule.requestvote.RequestVoteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  raft.rpcmodule.requestvote.RequestVoteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new RequestVoteServiceMethodDescriptorSupplier("RequestVote"))
              .build();
        }
      }
    }
    return getRequestVoteMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RequestVoteServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestVoteServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestVoteServiceStub>() {
        @java.lang.Override
        public RequestVoteServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestVoteServiceStub(channel, callOptions);
        }
      };
    return RequestVoteServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RequestVoteServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestVoteServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestVoteServiceBlockingStub>() {
        @java.lang.Override
        public RequestVoteServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestVoteServiceBlockingStub(channel, callOptions);
        }
      };
    return RequestVoteServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RequestVoteServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestVoteServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestVoteServiceFutureStub>() {
        @java.lang.Override
        public RequestVoteServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestVoteServiceFutureStub(channel, callOptions);
        }
      };
    return RequestVoteServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   *声明一个服务名称
   * </pre>
   */
  public static abstract class RequestVoteServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void requestVote(raft.rpcmodule.requestvote.RequestVoteRequest request,
        io.grpc.stub.StreamObserver<raft.rpcmodule.requestvote.RequestVoteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRequestVoteMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRequestVoteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                raft.rpcmodule.requestvote.RequestVoteRequest,
                raft.rpcmodule.requestvote.RequestVoteResponse>(
                  this, METHODID_REQUEST_VOTE)))
          .build();
    }
  }

  /**
   * <pre>
   *声明一个服务名称
   * </pre>
   */
  public static final class RequestVoteServiceStub extends io.grpc.stub.AbstractAsyncStub<RequestVoteServiceStub> {
    private RequestVoteServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestVoteServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestVoteServiceStub(channel, callOptions);
    }

    /**
     */
    public void requestVote(raft.rpcmodule.requestvote.RequestVoteRequest request,
        io.grpc.stub.StreamObserver<raft.rpcmodule.requestvote.RequestVoteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRequestVoteMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   *声明一个服务名称
   * </pre>
   */
  public static final class RequestVoteServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<RequestVoteServiceBlockingStub> {
    private RequestVoteServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestVoteServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestVoteServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public raft.rpcmodule.requestvote.RequestVoteResponse requestVote(raft.rpcmodule.requestvote.RequestVoteRequest request) {
      return blockingUnaryCall(
          getChannel(), getRequestVoteMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   *声明一个服务名称
   * </pre>
   */
  public static final class RequestVoteServiceFutureStub extends io.grpc.stub.AbstractFutureStub<RequestVoteServiceFutureStub> {
    private RequestVoteServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestVoteServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestVoteServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<raft.rpcmodule.requestvote.RequestVoteResponse> requestVote(
        raft.rpcmodule.requestvote.RequestVoteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRequestVoteMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST_VOTE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RequestVoteServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RequestVoteServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST_VOTE:
          serviceImpl.requestVote((raft.rpcmodule.requestvote.RequestVoteRequest) request,
              (io.grpc.stub.StreamObserver<raft.rpcmodule.requestvote.RequestVoteResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class RequestVoteServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RequestVoteServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return raft.rpcmodule.requestvote.RequestVoteProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("RequestVoteService");
    }
  }

  private static final class RequestVoteServiceFileDescriptorSupplier
      extends RequestVoteServiceBaseDescriptorSupplier {
    RequestVoteServiceFileDescriptorSupplier() {}
  }

  private static final class RequestVoteServiceMethodDescriptorSupplier
      extends RequestVoteServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RequestVoteServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RequestVoteServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RequestVoteServiceFileDescriptorSupplier())
              .addMethod(getRequestVoteMethod())
              .build();
        }
      }
    }
    return result;
  }
}
