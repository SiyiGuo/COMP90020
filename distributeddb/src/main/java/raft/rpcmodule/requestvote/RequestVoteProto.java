// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: requestvote.proto

package raft.rpcmodule.requestvote;

public final class RequestVoteProto {
  private RequestVoteProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_raft_RequestVoteRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_raft_RequestVoteRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_raft_RequestVoteResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_raft_RequestVoteResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\021requestvote.proto\022\004raft\"\"\n\022RequestVote" +
      "Request\022\014\n\004name\030\001 \001(\t\"&\n\023RequestVoteResp" +
      "onse\022\017\n\007message\030\001 \001(\t2X\n\022RequestVoteServ" +
      "ice\022B\n\013RequestVote\022\030.raft.RequestVoteReq" +
      "uest\032\031.raft.RequestVoteResponseB0\n\032raft." +
      "rpcmodule.requestvoteB\020RequestVoteProtoP" +
      "\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_raft_RequestVoteRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_raft_RequestVoteRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_raft_RequestVoteRequest_descriptor,
        new java.lang.String[] { "Name", });
    internal_static_raft_RequestVoteResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_raft_RequestVoteResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_raft_RequestVoteResponse_descriptor,
        new java.lang.String[] { "Message", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}