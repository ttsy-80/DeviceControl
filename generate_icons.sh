#!/bin/bash
# 图标生成脚本 - 如果系统有ImageMagick或sips工具，可以使用此脚本生成PNG图标

echo "生成Android应用图标..."

# 检查是否有sips (macOS)
if command -v sips &> /dev/null; then
    echo "使用sips工具生成图标..."
    
    # 创建临时SVG（简化版）
    # 由于sips不支持SVG，我们需要其他方法
    echo "请使用Android Studio的图标生成工具，或安装ImageMagick"
    
elif command -v convert &> /dev/null; then
    echo "使用ImageMagick生成图标..."
    
    densities=(
        "mdpi:48"
        "hdpi:72"
        "xhdpi:96"
        "xxhdpi:144"
        "xxxhdpi:192"
    )
    
    for density in "${densities[@]}"; do
        IFS=':' read -r name size <<< "$density"
        dir="app/src/main/res/mipmap-${name}"
        
        # 创建简单的PNG图标
        convert -size ${size}x${size} xc:'#6200EE' \
            -fill white -draw "circle $(($size/2)),$(($size/2)) $(($size/2)),$(($size/3))" \
            -fill '#6200EE' -draw "circle $(($size/2)),$(($size/2)) $(($size/2)),$(($size/5))" \
            "${dir}/ic_launcher.png"
        
        cp "${dir}/ic_launcher.png" "${dir}/ic_launcher_round.png"
        
        echo "生成 ${name} 图标 (${size}x${size})"
    done
    
    echo "图标生成完成！"
else
    echo "未找到图像处理工具。"
    echo "建议："
    echo "1. 使用Android Studio: Right-click res -> New -> Image Asset"
    echo "2. 安装ImageMagick: brew install imagemagick"
    echo "3. 使用在线工具生成图标"
fi
