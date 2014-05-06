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
		tc.x + offsetX * dir.x * resolution.x, 
		tc.y + offsetY * dir.y * resolution.y
	);
	if (coords.x > 1.0 || coords.x < 0.0 || coords.y > 1.0 || coords.y < 0.0) {
		return vec4(0.0, 0.0, 0.0, 0.0);
	}
	return texture2D(u_texture, coords);
}

vec4 blend(vec4 a, vec4 b, float factor)
{
	return vec4(
		max(a.r, b.r * factor), 
		max(a.g, b.g * factor), 
		max(a.b, b.b * factor), 
		1.0
	);
}

void main()
{
	vec4 total = vec4(0.0, 0.0, 0.0, 0.0);
	
	total = sample(0.0, 0.0);
	total = blend(total, sample(-32.0, -32.0), 0.00283);
	total = blend(total, sample(-31.0, -31.0), 0.00411);
	total = blend(total, sample(-30.0, -30.0), 0.00589);
	total = blend(total, sample(-29.0, -29.0), 0.00835);
	total = blend(total, sample(-28.0, -28.0), 0.01168);
	total = blend(total, sample(-27.0, -27.0), 0.01614);
	total = blend(total, sample(-26.0, -26.0), 0.02204);
	total = blend(total, sample(-25.0, -25.0), 0.02972);
	total = blend(total, sample(-24.0, -24.0), 0.03960);
	total = blend(total, sample(-23.0, -23.0), 0.05212);
	total = blend(total, sample(-22.0, -22.0), 0.06776);
	total = blend(total, sample(-21.0, -21.0), 0.08703);
	total = blend(total, sample(-20.0, -20.0), 0.11043);
	total = blend(total, sample(-19.0, -19.0), 0.13840);
	total = blend(total, sample(-18.0, -18.0), 0.17137);
	total = blend(total, sample(-17.0, -17.0), 0.20961);
	total = blend(total, sample(-16.0, -16.0), 0.25327);
	total = blend(total, sample(-15.0, -15.0), 0.30231);
	total = blend(total, sample(-14.0, -14.0), 0.35647);
	total = blend(total, sample(-13.0, -13.0), 0.41523);
	total = blend(total, sample(-12.0, -12.0), 0.47781);
	total = blend(total, sample(-11.0, -11.0), 0.54315);
	total = blend(total, sample(-10.0, -10.0), 0.60994);
	total = blend(total, sample(-9.0,  -9.0),  0.67663);
	total = blend(total, sample(-8.0,  -8.0),  0.74150);
	total = blend(total, sample(-7.0,  -7.0),  0.80273);
	total = blend(total, sample(-6.0,  -6.0),  0.85848);
	total = blend(total, sample(-5.0,  -5.0),  0.90696);
	total = blend(total, sample(-4.0,  -4.0),  0.94654);
    total = blend(total, sample(-3.0,  -3.0),  0.97588);
    total = blend(total, sample(-2.0,  -2.0),  0.99391);
    total = blend(total, sample(-1.0,  -1.0),  1.00000);
    total = blend(total, sample(1.0,   1.0),   1.00000);
    total = blend(total, sample(2.0,   2.0),   0.99391);
    total = blend(total, sample(3.0,   3.0),   0.97588);
    total = blend(total, sample(4.0,   4.0),   0.94654);
    total = blend(total, sample(5.0,   5.0),   0.90696);
    total = blend(total, sample(6.0,   6.0),   0.85848);
    total = blend(total, sample(7.0,   7.0),   0.80273);
    total = blend(total, sample(8.0,   8.0),   0.74150);
    total = blend(total, sample(9.0,   9.0),   0.67663);
    total = blend(total, sample(10.0,  10.0),  0.60994);
    total = blend(total, sample(11.0,  11.0),  0.54315);
    total = blend(total, sample(12.0,  12.0),  0.47781);
    total = blend(total, sample(13.0,  13.0),  0.41523);
    total = blend(total, sample(14.0,  14.0),  0.35647);
    total = blend(total, sample(15.0,  15.0),  0.30231);
    total = blend(total, sample(16.0,  16.0),  0.25327);
    total = blend(total, sample(17.0,  17.0),  0.20961);
    total = blend(total, sample(18.0,  18.0),  0.17137);
    total = blend(total, sample(19.0,  19.0),  0.13840);
    total = blend(total, sample(20.0,  20.0),  0.11043);
    total = blend(total, sample(21.0,  21.0),  0.08703);
    total = blend(total, sample(22.0,  22.0),  0.06776);
    total = blend(total, sample(23.0,  23.0),  0.05212);
    total = blend(total, sample(24.0,  24.0),  0.03960);
    total = blend(total, sample(25.0,  25.0),  0.02972);
    total = blend(total, sample(26.0,  26.0),  0.02204);
    total = blend(total, sample(27.0,  27.0),  0.01614);
    total = blend(total, sample(28.0,  28.0),  0.01168);
    total = blend(total, sample(29.0,  29.0),  0.00835);
    total = blend(total, sample(30.0,  30.0),  0.00589);
    total = blend(total, sample(31.0,  31.0),  0.00411);
    total = blend(total, sample(32.0,  32.0),  0.00283);
                                                                                  
	gl_FragColor = total;
}