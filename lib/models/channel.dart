import 'stream.dart';

class ChannelModel {
  final String id;
  final String name;
  final String shortName;
  final String logo;
  final String quality;
  final String? description;
  final String? category;
  final List<StreamModel> streams;

  ChannelModel({
    required this.id,
    required this.name,
    required this.shortName,
    required this.logo,
    required this.quality,
    this.description,
    this.category,
    required this.streams,
  });

  factory ChannelModel.fromJson(Map<String, dynamic> json) {
    return ChannelModel(
      id: json['id'] as String,
      name: json['name'] as String,
      shortName: json['shortName'] as String,
      logo: json['logo'] as String,
      quality: json['quality'] as String,
      description: json['description'] as String?,
      category: json['category'] as String?,
      streams: (json['streams'] as List<dynamic>)
          .map((e) => StreamModel.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'shortName': shortName,
      'logo': logo,
      'quality': quality,
      if (description != null) 'description': description,
      if (category != null) 'category': category,
      'streams': streams.map((e) => e.toJson()).toList(),
    };
  }

  ChannelModel copyWith({
    String? id,
    String? name,
    String? shortName,
    String? logo,
    String? quality,
    String? description,
    String? category,
    List<StreamModel>? streams,
  }) {
    return ChannelModel(
      id: id ?? this.id,
      name: name ?? this.name,
      shortName: shortName ?? this.shortName,
      logo: logo ?? this.logo,
      quality: quality ?? this.quality,
      description: description ?? this.description,
      category: category ?? this.category,
      streams: streams ?? this.streams,
    );
  }
}
