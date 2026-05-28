package com.bytecamp.experiencefeed;

final class ImagePost {
    final String id;
    final String title;
    final String description;
    final String dateLabel;
    final String location;
    final String imageUrl;
    final int previewHeightDp;

    ImagePost(
            String id,
            String title,
            String description,
            String dateLabel,
            String location,
            String imageUrl,
            int previewHeightDp
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateLabel = dateLabel;
        this.location = location;
        this.imageUrl = imageUrl;
        this.previewHeightDp = previewHeightDp;
    }
}
