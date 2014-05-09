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
		min(a.r + b.r * factor, 1.0), 
		a.g, 
		a.b, 
		1.0
	);
}

float attenuate(float x, float y) 
{
	float modFactor = (1.0 - (0.55 * (1.0 - sample(x, y).g)));
	return modFactor;
}

void main()
{
	float attenuationFactor = 1.0;
	vec4 total = vec4(0.0, 0.0, 0.0, 0.0);
	
	attenuationFactor *= attenuate(0.0, 0.0);
	total = sample(0.0,   0.0);

	attenuationFactor *= attenuate(1.0, 1.0);
    total = blend(total, sample(1.0,   1.0),   0.99307 * attenuationFactor);
    
	attenuationFactor *= attenuate(2.0, 2.0);
    total = blend(total, sample(2.0,   2.0),   0.97260 * attenuationFactor);
    
	attenuationFactor *= attenuate(3.0, 3.0);
    total = blend(total, sample(3.0,   3.0),   0.93941 * attenuationFactor);
    
	attenuationFactor *= attenuate(4.0, 4.0);
    total = blend(total, sample(4.0,   4.0),   0.89483 * attenuationFactor);
    
	attenuationFactor *= attenuate(5.0, 5.0);
    total = blend(total, sample(5.0,   5.0),   0.84062 * attenuationFactor);
    
	attenuationFactor *= attenuate(6.0, 6.0);
    total = blend(total, sample(6.0,   6.0),   0.77880 * attenuationFactor);
    
	attenuationFactor *= attenuate(7.0, 7.0);
    total = blend(total, sample(7.0,   7.0),   0.71157 * attenuationFactor);
    
	attenuationFactor *= attenuate(8.0, 8.0);
    total = blend(total, sample(8.0,   8.0),   0.64118 * attenuationFactor);
    
	attenuationFactor *= attenuate(9.0, 9.0);
    total = blend(total, sample(9.0,   9.0),   0.56978 * attenuationFactor);
    
	attenuationFactor *= attenuate(10.0, 10.0);
    total = blend(total, sample(10.0,  10.0),  0.49935 * attenuationFactor);
    
	attenuationFactor *= attenuate(11.0, 11.0);
    total = blend(total, sample(11.0,  11.0),  0.43159 * attenuationFactor);
    
	attenuationFactor *= attenuate(12.0, 12.0);
    total = blend(total, sample(12.0,  12.0),  0.36787 * attenuationFactor);
    
	attenuationFactor *= attenuate(13.0, 13.0);
    total = blend(total, sample(13.0,  13.0),  0.30924 * attenuationFactor);
    
	attenuationFactor *= attenuate(14.0, 14.0);
    total = blend(total, sample(14.0,  14.0),  0.25637 * attenuationFactor);
    
	attenuationFactor *= attenuate(15.0, 15.0);
    total = blend(total, sample(15.0,  15.0),  0.20961 * attenuationFactor);
    
	attenuationFactor *= attenuate(16.0, 16.0);
    total = blend(total, sample(16.0,  16.0),  0.16901 * attenuationFactor);
    
	attenuationFactor *= attenuate(17.0, 17.0);
    total = blend(total, sample(17.0,  17.0),  0.13439 * attenuationFactor);
    
	attenuationFactor *= attenuate(18.0, 18.0);
    total = blend(total, sample(18.0,  18.0),  0.10539 * attenuationFactor);
    
	attenuationFactor *= attenuate(19.0, 19.0);
    total = blend(total, sample(19.0,  19.0),  0.08151 * attenuationFactor);
    
	attenuationFactor *= attenuate(20.0, 20.0);
    total = blend(total, sample(20.0,  20.0),  0.06217 * attenuationFactor);
    
	attenuationFactor *= attenuate(21.0, 21.0);
    total = blend(total, sample(21.0,  21.0),  0.04677 * attenuationFactor);
    
	attenuationFactor *= attenuate(22.0, 22.0);
    total = blend(total, sample(22.0,  22.0),  0.03469 * attenuationFactor);
    
	attenuationFactor *= attenuate(23.0, 23.0);
    total = blend(total, sample(23.0,  23.0),  0.02538 * attenuationFactor);
    
	attenuationFactor *= attenuate(24.0, 24.0);
    total = blend(total, sample(24.0,  24.0),  0.01831 * attenuationFactor);
 
 
    attenuationFactor = 1.0;
	attenuationFactor *= attenuate(-1.0, -1.0);
    total = blend(total, sample(-1.0,  -1.0),  0.99307 * attenuationFactor);
    
	attenuationFactor *= attenuate(-2.0, -2.0);
    total = blend(total, sample(-2.0,  -2.0),  0.97260 * attenuationFactor);
    
	attenuationFactor *= attenuate(-3.0, -3.0);
    total = blend(total, sample(-3.0,  -3.0),  0.93941 * attenuationFactor);
    
	attenuationFactor *= attenuate(-4.0, -4.0);
	total = blend(total, sample(-4.0,  -4.0),  0.89483 * attenuationFactor);
	
	attenuationFactor *= attenuate(-5.0, -5.0);
	total = blend(total, sample(-5.0,  -5.0),  0.84062 * attenuationFactor);
	
	attenuationFactor *= attenuate(-6.0, -6.0);
	total = blend(total, sample(-6.0,  -6.0),  0.77880 * attenuationFactor);
	
	attenuationFactor *= attenuate(-7.0, -7.0);
	total = blend(total, sample(-7.0,  -7.0),  0.71157 * attenuationFactor);
	
	attenuationFactor *= attenuate(-8.0, -8.0);
	total = blend(total, sample(-8.0,  -8.0),  0.64118 * attenuationFactor);
	
	attenuationFactor *= attenuate(-9.0, -9.0);
	total = blend(total, sample(-9.0,  -9.0),  0.56978 * attenuationFactor);
	
	attenuationFactor *= attenuate(-10.0, -10.0);
	total = blend(total, sample(-10.0, -10.0), 0.49935 * attenuationFactor);
	
	attenuationFactor *= attenuate(-11.0, -11.0);
	total = blend(total, sample(-11.0, -11.0), 0.43159 * attenuationFactor);
	
	attenuationFactor *= attenuate(-12.0, -12.0);
	total = blend(total, sample(-12.0, -12.0), 0.36787 * attenuationFactor);
	
	attenuationFactor *= attenuate(-13.0, -13.0);
	total = blend(total, sample(-13.0, -13.0), 0.30924 * attenuationFactor);
	
	attenuationFactor *= attenuate(-14.0, -14.0);
	total = blend(total, sample(-14.0, -14.0), 0.25637 * attenuationFactor);
	
	attenuationFactor *= attenuate(-15.0, -15.0);
	total = blend(total, sample(-15.0, -15.0), 0.20961 * attenuationFactor);
	
	attenuationFactor *= attenuate(-16.0, -16.0);
	total = blend(total, sample(-16.0, -16.0), 0.16901 * attenuationFactor);
	
	attenuationFactor *= attenuate(-17.0, -17.0);
	total = blend(total, sample(-17.0, -17.0), 0.13439 * attenuationFactor);
	
	attenuationFactor *= attenuate(-18.0, -18.0);
	total = blend(total, sample(-18.0, -18.0), 0.10539 * attenuationFactor);
	
	attenuationFactor *= attenuate(-19.0, -19.0);
	total = blend(total, sample(-19.0, -19.0), 0.08151 * attenuationFactor);
	
	attenuationFactor *= attenuate(-20.0, -20.0);
	total = blend(total, sample(-20.0, -20.0), 0.06217 * attenuationFactor);
	
	attenuationFactor *= attenuate(-21.0, -21.0);
	total = blend(total, sample(-21.0, -21.0), 0.04677 * attenuationFactor);
	
	attenuationFactor *= attenuate(-22.0, -22.0);
	total = blend(total, sample(-22.0, -22.0), 0.03469 * attenuationFactor);
	
	attenuationFactor *= attenuate(-23.0, -23.0);
	total = blend(total, sample(-23.0, -23.0), 0.02538 * attenuationFactor);
	
	attenuationFactor *= attenuate(-24.0, -24.0);
	total = blend(total, sample(-24.0, -24.0), 0.01831 * attenuationFactor);
	
	gl_FragColor = total;
}