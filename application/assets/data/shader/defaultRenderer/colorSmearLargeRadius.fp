#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D foreground;
uniform vec2 res;
uniform vec2 dir;

vec4 sample(float offsetX, float offsetY, sampler2D from) 
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
	return texture2D(from, coords);
}

vec4 blend(vec4 a, vec4 b, float factor)
{
	return vec4(
		min(a.r + b.r * factor, 1.0), 
		min(a.g + b.g * factor, 1.0), 
		min(a.b + b.b * factor, 1.0), 
		1.0
	);
}

float attenuate(float x, float y) 
{
	float modFactor = (1.0 - (0.3 * sample(x, y, foreground).g));
	return modFactor;
}

void main()
{
	vec4 total = sample(0.0, 0.0, u_texture);

	float attenuationFactor = 1.0;
	attenuationFactor *= attenuate(1.0, 1.0);
    total = blend(total, sample(1.0,   1.0, u_texture),   1.00000 * 1.00);
    
	attenuationFactor *= attenuate(2.0, 2.0);
    total = blend(total, sample(2.0,   2.0, u_texture),   0.99391 * 1.00);
    
	attenuationFactor *= attenuate(3.0, 3.0);
    total = blend(total, sample(3.0,   3.0, u_texture),   0.97588 * 1.00);
    
	attenuationFactor *= attenuate(4.0, 4.0);
    total = blend(total, sample(4.0,   4.0, u_texture),   0.94654 * 1.00);
    
	attenuationFactor *= attenuate(5.0, 5.0);
    total = blend(total, sample(5.0,   5.0, u_texture),   0.90696 * 1.00);
    
	attenuationFactor *= attenuate(6.0, 6.0);
    total = blend(total, sample(6.0,   6.0, u_texture),   0.85848 * 1.00);
    
	attenuationFactor *= attenuate(7.0, 7.0);
    total = blend(total, sample(7.0,   7.0, u_texture),   0.80273 * 1.00);
    
	attenuationFactor *= attenuate(8.0, 8.0);
    total = blend(total, sample(8.0,   8.0, u_texture),   0.74150 * 1.00);
    
	attenuationFactor *= attenuate(9.0, 9.0);
    total = blend(total, sample(9.0,   9.0, u_texture),   0.67663 * 1.00);
    
	attenuationFactor *= attenuate(10.0, 10.0);
    total = blend(total, sample(10.0,  10.0, u_texture),  0.60994 * 1.00);
    
	attenuationFactor *= attenuate(11.0, 11.0);
    total = blend(total, sample(11.0,  11.0, u_texture),  0.54315 * 1.00);
    
	attenuationFactor *= attenuate(12.0, 12.0);
    total = blend(total, sample(12.0,  12.0, u_texture),  0.47781 * 1.00);
    
	attenuationFactor *= attenuate(13.0, 13.0);
    total = blend(total, sample(13.0,  13.0, u_texture),  0.41523 * 1.00);
    
	attenuationFactor *= attenuate(14.0, 14.0);
    total = blend(total, sample(14.0,  14.0, u_texture),  0.35647 * 1.00);
    
	attenuationFactor *= attenuate(15.0, 15.0);
    total = blend(total, sample(15.0,  15.0, u_texture),  0.30231 * 1.00);
    
	attenuationFactor *= attenuate(16.0, 16.0);
    total = blend(total, sample(16.0,  16.0, u_texture),  0.25327 * 1.00);
    
	attenuationFactor *= attenuate(17.0, 17.0);
    total = blend(total, sample(17.0,  17.0, u_texture),  0.20961 * 1.00);
    
	attenuationFactor *= attenuate(18.0, 18.0);
    total = blend(total, sample(18.0,  18.0, u_texture),  0.17137 * 1.00);
    
	attenuationFactor *= attenuate(19.0, 19.0);
    total = blend(total, sample(19.0,  19.0, u_texture),  0.13840 * 1.00);
    
	attenuationFactor *= attenuate(20.0, 20.0);
    total = blend(total, sample(20.0,  20.0, u_texture),  0.11043 * 1.00);
    
	attenuationFactor *= attenuate(21.0, 21.0);
    total = blend(total, sample(21.0,  21.0, u_texture),  0.08703 * 1.00);
    
	attenuationFactor *= attenuate(22.0, 22.0);
    total = blend(total, sample(22.0,  22.0, u_texture),  0.06776 * 1.00);
    
	attenuationFactor *= attenuate(23.0, 23.0);
    total = blend(total, sample(23.0,  23.0, u_texture),  0.05212 * 1.00);
    
	attenuationFactor *= attenuate(24.0, 24.0);
    total = blend(total, sample(24.0,  24.0, u_texture),  0.03960 * 1.00);
    
	attenuationFactor *= attenuate(25.0, 25.0);
    total = blend(total, sample(25.0,  25.0, u_texture),  0.02972 * 1.00);
    
	attenuationFactor *= attenuate(26.0, 26.0);
    total = blend(total, sample(26.0,  26.0, u_texture),  0.02204 * 1.00);
    
	attenuationFactor *= attenuate(27.0, 27.0);
    total = blend(total, sample(27.0,  27.0, u_texture),  0.01614 * 1.00);
    
	attenuationFactor *= attenuate(28.0, 28.0);
    total = blend(total, sample(28.0,  28.0, u_texture),  0.01168 * 1.00);
    
	attenuationFactor *= attenuate(29.0, 29.0);
    total = blend(total, sample(29.0,  29.0, u_texture),  0.00835 * 1.00);
    
	attenuationFactor *= attenuate(30.0, 30.0);
    total = blend(total, sample(30.0,  30.0, u_texture),  0.00589 * 1.00);
    
	attenuationFactor *= attenuate(31.0, 31.0);
    total = blend(total, sample(31.0,  31.0, u_texture),  0.00411 * 1.00);
    
	attenuationFactor *= attenuate(32.0, 32.0);
    total = blend(total, sample(32.0,  32.0, u_texture),  0.00283 * 1.00);
 
 
    attenuationFactor *= 1.0;
	attenuationFactor *= attenuate(-1.0, -1.0);
    total = blend(total, sample(-1.0,  -1.0, u_texture),  1.00000 * 1.00);
    
	attenuationFactor *= attenuate(-2.0, -2.0);
    total = blend(total, sample(-2.0,  -2.0, u_texture),  0.99391 * 1.00);
    
	attenuationFactor *= attenuate(-3.0, -3.0);
    total = blend(total, sample(-3.0,  -3.0, u_texture),  0.97588 * 1.00);
    
	attenuationFactor *= attenuate(-4.0, -4.0);
	total = blend(total, sample(-4.0,  -4.0, u_texture),  0.94654 * 1.00);
	
	attenuationFactor *= attenuate(-5.0, -5.0);
	total = blend(total, sample(-5.0,  -5.0, u_texture),  0.90696 * 1.00);
	
	attenuationFactor *= attenuate(-6.0, -6.0);
	total = blend(total, sample(-6.0,  -6.0, u_texture),  0.85848 * 1.00);
	
	attenuationFactor *= attenuate(-7.0, -7.0);
	total = blend(total, sample(-7.0,  -7.0, u_texture),  0.80273 * 1.00);
	
	attenuationFactor *= attenuate(-8.0, -8.0);
	total = blend(total, sample(-8.0,  -8.0, u_texture),  0.74150 * 1.00);
	
	attenuationFactor *= attenuate(-9.0, -9.0);
	total = blend(total, sample(-9.0,  -9.0, u_texture),  0.67663 * 1.00);
	
	attenuationFactor *= attenuate(-10.0, -10.0);
	total = blend(total, sample(-10.0, -10.0, u_texture), 0.60994 * 1.00);
	
	attenuationFactor *= attenuate(-11.0, -11.0);
	total = blend(total, sample(-11.0, -11.0, u_texture), 0.54315 * 1.00);
	
	attenuationFactor *= attenuate(-12.0, -12.0);
	total = blend(total, sample(-12.0, -12.0, u_texture), 0.47781 * 1.00);
	
	attenuationFactor *= attenuate(-13.0, -13.0);
	total = blend(total, sample(-13.0, -13.0, u_texture), 0.41523 * 1.00);
	
	attenuationFactor *= attenuate(-14.0, -14.0);
	total = blend(total, sample(-14.0, -14.0, u_texture), 0.35647 * 1.00);
	
	attenuationFactor *= attenuate(-15.0, -15.0);
	total = blend(total, sample(-15.0, -15.0, u_texture), 0.30231 * 1.00);
	
	attenuationFactor *= attenuate(-16.0, -16.0);
	total = blend(total, sample(-16.0, -16.0, u_texture), 0.25327 * 1.00);
	
	attenuationFactor *= attenuate(-17.0, -17.0);
	total = blend(total, sample(-17.0, -17.0, u_texture), 0.20961 * 1.00);
	
	attenuationFactor *= attenuate(-18.0, -18.0);
	total = blend(total, sample(-18.0, -18.0, u_texture), 0.17137 * 1.00);
	
	attenuationFactor *= attenuate(-19.0, -19.0);
	total = blend(total, sample(-19.0, -19.0, u_texture), 0.13840 * 1.00);
	
	attenuationFactor *= attenuate(-20.0, -20.0);
	total = blend(total, sample(-20.0, -20.0, u_texture), 0.11043 * 1.00);
	
	attenuationFactor *= attenuate(-21.0, -21.0);
	total = blend(total, sample(-21.0, -21.0, u_texture), 0.08703 * 1.00);
	
	attenuationFactor *= attenuate(-22.0, -22.0);
	total = blend(total, sample(-22.0, -22.0, u_texture), 0.06776 * 1.00);
	
	attenuationFactor *= attenuate(-23.0, -23.0);
	total = blend(total, sample(-23.0, -23.0, u_texture), 0.05212 * 1.00);
	
	attenuationFactor *= attenuate(-24.0, -24.0);
	total = blend(total, sample(-24.0, -24.0, u_texture), 0.03960 * 1.00);
	
	attenuationFactor *= attenuate(-25.0, -25.0);
	total = blend(total, sample(-25.0, -25.0, u_texture), 0.02972 * 1.00);
	
	attenuationFactor *= attenuate(-26.0, -26.0);
	total = blend(total, sample(-26.0, -26.0, u_texture), 0.02204 * 1.00);
	
	attenuationFactor *= attenuate(-27.0, -27.0);
	total = blend(total, sample(-27.0, -27.0, u_texture), 0.01614 * 1.00);
	
	attenuationFactor *= attenuate(-28.0, -28.0);
	total = blend(total, sample(-28.0, -28.0, u_texture), 0.01168 * 1.00);
	
	attenuationFactor *= attenuate(-29.0, -29.0);
	total = blend(total, sample(-29.0, -29.0, u_texture), 0.00835 * 1.00);
	
	attenuationFactor *= attenuate(-30.0, -30.0);
	total = blend(total, sample(-30.0, -30.0, u_texture), 0.00589 * 1.00);
	
	attenuationFactor *= attenuate(-31.0, -31.0);
	total = blend(total, sample(-31.0, -31.0, u_texture), 0.00411 * 1.00);
	
	attenuationFactor *= attenuate(-32.0, -32.0);
    total = blend(total, sample(-32.0, -32.0, u_texture), 0.00283 * 1.00);
                                                                                  
	gl_FragColor = sample(8458.0, 0.0, foreground);
}