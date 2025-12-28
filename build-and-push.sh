#!/bin/bash

# Exit on error
set -e

# 멀티 아키텍처 이미지 빌드 및 푸시 스크립트

IMAGE_NAME="ddingsh9/auth-server"
VERSION="1.0.0"

echo "Building and pushing multi-architecture Docker image..."
echo "Image: ${IMAGE_NAME}:${VERSION}"
echo "Platforms: linux/amd64, linux/arm64"

# buildx builder 생성 (없으면)
docker buildx create --name multiarch-builder --use 2>/dev/null || docker buildx use multiarch-builder

# buildx bootstrap
docker buildx inspect --bootstrap

# 멀티 아키텍처 빌드 및 푸시
echo "Starting build..."
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t ${IMAGE_NAME}:${VERSION} \
  -t ${IMAGE_NAME}:latest \
  --push \
  .

# Only print success if we get here (set -e will exit on error)
echo ""
echo "✓ Build and push completed successfully!"
echo "✓ Image pushed: ${IMAGE_NAME}:${VERSION}"
echo "✓ Image pushed: ${IMAGE_NAME}:latest"

# 로컬 이미지 정리
echo ""
echo "Cleaning up local images..."

# 로컬에 있는 해당 이미지 삭제
docker rmi ${IMAGE_NAME}:${VERSION} 2>/dev/null && echo "✓ Removed local image: ${IMAGE_NAME}:${VERSION}" || echo "- No local image found: ${IMAGE_NAME}:${VERSION}"
docker rmi ${IMAGE_NAME}:latest 2>/dev/null && echo "✓ Removed local image: ${IMAGE_NAME}:latest" || echo "- No local image found: ${IMAGE_NAME}:latest"

# dangling 이미지 정리 (빌드 과정에서 생긴 <none> 이미지들)
docker image prune -f >/dev/null 2>&1 && echo "✓ Cleaned up dangling images"

echo ""
echo "✓ Local cleanup completed!"
