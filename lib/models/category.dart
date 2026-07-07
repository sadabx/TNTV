import 'channel.dart';

class CategoryModel {
  final String name;
  final List<ChannelModel> channels;

  CategoryModel({
    required this.name,
    required this.channels,
  });

  factory CategoryModel.fromJson(Map<String, dynamic> json) {
    return CategoryModel(
      name: json['name'] as String,
      channels: (json['channels'] as List<dynamic>)
          .map((e) => ChannelModel.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'channels': channels.map((e) => e.toJson()).toList(),
    };
  }
}
