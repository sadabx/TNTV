class StreamModel {
  final String label;
  final String url;
  final String? bitrate;

  StreamModel({
    required this.label,
    required this.url,
    this.bitrate,
  });

  factory StreamModel.fromJson(Map<String, dynamic> json) {
    return StreamModel(
      label: json['label'] as String,
      url: json['url'] as String,
      bitrate: json['bitrate'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'label': label,
      'url': url,
      if (bitrate != null) 'bitrate': bitrate,
    };
  }
}
