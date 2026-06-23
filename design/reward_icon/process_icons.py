import os
import glob
from PIL import Image
from collections import deque

def remove_white_bg_flood_fill(img, tolerance=30):
    img = img.convert("RGBA")
    data = img.load()
    width, height = img.size
    
    # We will use BFS from edges
    queue = deque()
    # Add borders to queue
    for x in range(width):
        queue.append((x, 0))
        queue.append((x, height - 1))
    for y in range(height):
        queue.append((0, y))
        queue.append((width - 1, y))
        
    visited = set(queue)
    
    while queue:
        x, y = queue.popleft()
        r, g, b, a = data[x, y]
        
        # Check if color is close to white
        if a > 0 and r >= 255 - tolerance and g >= 255 - tolerance and b >= 255 - tolerance:
            data[x, y] = (255, 255, 255, 0)
            
            # Add neighbors
            for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                nx, ny = x + dx, y + dy
                if 0 <= nx < width and 0 <= ny < height and (nx, ny) not in visited:
                    visited.add((nx, ny))
                    queue.append((nx, ny))
                    
    return img

def process_images():
    source_dir = r"d:\Users\rrrrz\Desktop\reward_icon"
    output_dir = os.path.join(source_dir, "processed_icons")
    os.makedirs(output_dir, exist_ok=True)
    
    ref_image_path = os.path.join(source_dir, "ChatGPT Image 2026年5月11日 22_45_04 (1).png")
    if not os.path.exists(ref_image_path):
        print(f"Reference image not found: {ref_image_path}")
        return
        
    print("Processing reference image...")
    ref_img = Image.open(ref_image_path)
    ref_transparent = remove_white_bg_flood_fill(ref_img)
    ref_bbox = ref_transparent.getbbox()
    
    if ref_bbox:
        ref_content_w = ref_bbox[2] - ref_bbox[0]
        ref_content_h = ref_bbox[3] - ref_bbox[1]
        print(f"Reference bounding box: {ref_bbox}, size: {ref_content_w}x{ref_content_h}")
    else:
        ref_content_w, ref_content_h = ref_img.size
        print(f"Reference is entirely transparent? Size: {ref_img.size}")
        
    target_canvas_size = ref_img.size
    print(f"Target canvas size: {target_canvas_size}")
    
    images = glob.glob(os.path.join(source_dir, "*.png"))
    count = 1
    
    for img_path in images:
        if "processed_icons" in img_path:
            continue
            
        print(f"Processing {os.path.basename(img_path)}...")
        img = Image.open(img_path)
        img_trans = remove_white_bg_flood_fill(img)
        bbox = img_trans.getbbox()
        
        if bbox:
            cropped = img_trans.crop(bbox)
            crop_w, crop_h = cropped.size
            
            # Scale to match reference content size
            scale = min(ref_content_w / crop_w, ref_content_h / crop_h)
            new_w = int(crop_w * scale)
            new_h = int(crop_h * scale)
            
            try:
                resample_filter = Image.Resampling.LANCZOS
            except AttributeError:
                resample_filter = Image.LANCZOS
            resized = cropped.resize((new_w, new_h), resample_filter)
            
            # Paste into new transparent canvas
            new_img = Image.new("RGBA", target_canvas_size, (255, 255, 255, 0))
            
            # Center the image
            paste_x = (target_canvas_size[0] - new_w) // 2
            paste_y = (target_canvas_size[1] - new_h) // 2
            
            new_img.paste(resized, (paste_x, paste_y), resized)
        else:
            new_img = Image.new("RGBA", target_canvas_size, (255, 255, 255, 0))
            
        output_name = f"reward_icon_{count:02d}.png"
        new_img.save(os.path.join(output_dir, output_name))
        count += 1
        
    print(f"Successfully processed {count-1} icons into {output_dir}")

if __name__ == "__main__":
    process_images()
