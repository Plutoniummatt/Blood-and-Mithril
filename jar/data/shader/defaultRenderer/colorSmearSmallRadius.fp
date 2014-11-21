#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 res;
uniform vec2 dir;

vec4 sample(float offsetX, float offsetY) 
{
	vec2 tc = v_texCoords;
	vec2 resolution = vec2(1.0/res.x, 1.0/res.y);
	vec2 coords = vec2(
		tc.x + (offsetX * dir.x * resolution.x), 
		tc.y + (offsetY * dir.y * resolution.y)
	);
	if (coords.x > 1.0 || coords.x < 0.0 || coords.y > 1.0 || coords.y < 0.0) {
		return vec4(0.0, 0.0, 0.0, 0.0);
	}
	return texture2D(u_texture, coords);
}

vec4 blend(vec4 a, vec4 b, float factor)
{
	return vec4(
		a.r, 
		min(a.g + b.g * factor, 1.0), 
		a.b, 
		1.0
	);
}

void main()
{
	vec4 total = vec4(0.0, 0.0, 0.0, 0.0);
	
	total = sample(0.0, 0.0);
    total = blend(total, sample(-2.0,  -2.0),  0.33);
    total = blend(total, sample(-1.0,  -1.0),  0.66);
    total = blend(total, sample(1.0,   1.0),   0.66);
    total = blend(total, sample(2.0,   2.0),   0.33);
                                                                                  
	gl_FragColor = total;
}