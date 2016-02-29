#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec4 override;
uniform vec2 topLeft;
uniform vec2 bottomRight;
uniform float feather;

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{
	float r = rand(v_texCoords)/256.0;
	float width = bottomRight.x - topLeft.x;
	float height = topLeft.y - bottomRight.y;

	float d = distance(
		v_texCoords, 
		vec2(
			topLeft.x + (width/2.0), 
			bottomRight.y + (height/2.0)
		)
	) / width;
	
	float alpha = 1.0;
	if (d > 0.5 - feather) {
		alpha = 1.0 - (d - (0.5 - feather)) / feather; 
	}
	
	if (d > 0.5) {
		alpha = 0.0;
	}
	
	gl_FragColor = vec4(override.rgb, alpha * override.a) + vec4(r, r, r, 0.0);
}