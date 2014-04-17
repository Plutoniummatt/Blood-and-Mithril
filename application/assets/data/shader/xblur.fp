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

vec4 invertSample(vec2 coord) 
{
	vec4 s = texture2D(u_texture, coord);
	return vec4(s.r, s.g, s.b, 1.0 - s.a);
}

void main()
{
	vec2 tc = v_texCoords;
	vec2 resolution = vec2(1.0/res.x, 1.0/res.y);
	vec4 total = vec4(0.0, 0.0, 0.0, 0.0);
	
	total += invertSample(vec2(tc.x - 19.0*dir.x*resolution.x, tc.y - 19.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 18.0*dir.x*resolution.x, tc.y - 18.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 17.0*dir.x*resolution.x, tc.y - 17.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 16.0*dir.x*resolution.x, tc.y - 16.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 15.0*dir.x*resolution.x, tc.y - 15.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 14.0*dir.x*resolution.x, tc.y - 14.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 13.0*dir.x*resolution.x, tc.y - 13.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 12.0*dir.x*resolution.x, tc.y - 12.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 11.0*dir.x*resolution.x, tc.y - 11.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 10.0*dir.x*resolution.x, tc.y - 10.0*dir.y*resolution.y)) * 0.02;
	
	total += invertSample(vec2(tc.x - 9.0*dir.x*resolution.x, tc.y - 9.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 8.0*dir.x*resolution.x, tc.y - 8.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 7.0*dir.x*resolution.x, tc.y - 7.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 6.0*dir.x*resolution.x, tc.y - 6.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 5.0*dir.x*resolution.x, tc.y - 5.0*dir.y*resolution.y)) * 0.02;
	total += invertSample(vec2(tc.x - 4.0*dir.x*resolution.x, tc.y - 4.0*dir.y*resolution.y)) * 0.05;
    total += invertSample(vec2(tc.x - 3.0*dir.x*resolution.x, tc.y - 3.0*dir.y*resolution.y)) * 0.05;
    total += invertSample(vec2(tc.x - 2.0*dir.x*resolution.x, tc.y - 2.0*dir.y*resolution.y)) * 0.05;
    total += invertSample(vec2(tc.x - 1.0*dir.x*resolution.x, tc.y - 1.0*dir.y*resolution.y)) * 0.05;

    total += invertSample(vec2(tc.x, tc.y)) * 0.06;

    total += invertSample(vec2(tc.x + 1.0*dir.x*resolution.x, tc.y + 1.0*dir.y*resolution.y)) * 0.05;
    total += invertSample(vec2(tc.x + 2.0*dir.x*resolution.x, tc.y + 2.0*dir.y*resolution.y)) * 0.05;
    total += invertSample(vec2(tc.x + 3.0*dir.x*resolution.x, tc.y + 3.0*dir.y*resolution.y)) * 0.05;
    total += invertSample(vec2(tc.x + 4.0*dir.x*resolution.x, tc.y + 4.0*dir.y*resolution.y)) * 0.05;
    total += invertSample(vec2(tc.x + 5.0*dir.x*resolution.x, tc.y + 5.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 6.0*dir.x*resolution.x, tc.y + 6.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 7.0*dir.x*resolution.x, tc.y + 7.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 8.0*dir.x*resolution.x, tc.y + 8.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 9.0*dir.x*resolution.x, tc.y + 9.0*dir.y*resolution.y)) * 0.02;
    
    total += invertSample(vec2(tc.x + 10.0*dir.x*resolution.x, tc.y + 10.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 11.0*dir.x*resolution.x, tc.y + 11.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 12.0*dir.x*resolution.x, tc.y + 12.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 13.0*dir.x*resolution.x, tc.y + 13.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 14.0*dir.x*resolution.x, tc.y + 14.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 15.0*dir.x*resolution.x, tc.y + 15.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 16.0*dir.x*resolution.x, tc.y + 16.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 17.0*dir.x*resolution.x, tc.y + 17.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 18.0*dir.x*resolution.x, tc.y + 18.0*dir.y*resolution.y)) * 0.02;
    total += invertSample(vec2(tc.x + 19.0*dir.x*resolution.x, tc.y + 19.0*dir.y*resolution.y)) * 0.02;
    
	gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0 - total.a);
}